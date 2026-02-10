package com.hello.lets.test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.BankCodes
import com.hello.lets.test.data.entity.BankAccount
import com.hello.lets.test.data.entity.Category
import com.hello.lets.test.data.entity.Transaction
import com.hello.lets.test.data.entity.TransactionType
import com.hello.lets.test.data.entity.UserProfile
import com.hello.lets.test.data.preferences.AppPreferences
import com.hello.lets.test.data.repository.TransactionRepository
import com.hello.lets.test.sms.SmsReader
import com.hello.lets.test.sms.TransactionParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for the Dashboard screen.
 * Manages transaction data, SMS sync, and UI state.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(
        database.transactionDao(),
        database.categoryDao(),
        database.parsingRuleDao(),
        database.excludedSenderDao(),
        database.budgetDao()
    )
    private val smsReader = SmsReader(application)
    private val transactionParser = TransactionParser()
    private val appPreferences = AppPreferences.getInstance(application)
    
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    // Current month date range
    private val currentMonthStart: Long
    private val currentMonthEnd: Long
    
    init {
        val calendar = Calendar.getInstance()
        
        // Start of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentMonthStart = calendar.timeInMillis
        
        // End of current month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        currentMonthEnd = calendar.timeInMillis
    }
    
    /**
     * Dashboard UI state combining all data streams.
     */
    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            repository.getTotalSpent(currentMonthStart, currentMonthEnd),
            repository.getTotalIncome(currentMonthStart, currentMonthEnd),
            repository.getRecentTransactions(5),
            repository.getTransactionCount()
        ) { spent, income, recentTransactions, count ->
            StatsData(spent, income, recentTransactions, count)
        },
        combine(
            database.userProfileDao().getProfile(),
            database.bankAccountDao().getAllAccounts()
        ) { profile, accounts ->
            UserData(profile, accounts)
        }
    ) { stats, user ->
        val budget = appPreferences.monthlyBudget.toDouble()
        DashboardUiState(
            totalSpent = stats.spent,
            totalIncome = stats.income,
            budget = budget,
            recentTransactions = stats.recentTransactions,
            transactionCount = stats.count,
            userName = user.profile?.name ?: "User",
            bankAccounts = user.accounts,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
    
    /**
     * All categories for display.
     */
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Sync SMS messages and extract transactions.
     */
    fun syncSms() {
        viewModelScope.launch {
            _syncState.update { it.copy(isSyncing = true, error = null, syncedCount = 0) }
            
            try {
                // Get excluded senders
                val excludedAddresses = repository.getExcludedAddresses()
                
                // Read bank SMS messages
                val messages = smsReader.readBankSms(500)
                
                // Get active parsing rules for category matching
                val rules = repository.getActiveRulesList()
                
                // Get all bank accounts to check for existence
                val existingAccounts = database.bankAccountDao().getAllAccountsList()
                
                var newTransactionCount = 0
                
                for (sms in messages) {
                    // Skip excluded senders
                    if (excludedAddresses.any { sms.address.contains(it, ignoreCase = true) }) {
                        continue
                    }
                    
                    // ===== SMART DEDUPLICATION =====
                    // Layer 1: Exact raw SMS body match (same message re-read)
                    val existingByRawSms = repository.findTransactionByRawSms(sms.body)
                    if (existingByRawSms != null) {
                        continue
                    }
                    
                    // Parse the SMS first so we can use parsed data for dedup
                    val parsed = transactionParser.parse(sms.body)
                    
                    // Only proceed if we extracted valid data
                    if (parsed.amount == null || parsed.transactionType == null) {
                        continue
                    }
                    
                    // Layer 2: Same UPI/IMPS/NEFT reference ID (bank SMS + UPI app SMS)
                    if (parsed.referenceId != null) {
                        val existingByRef = repository.findTransactionByReferenceId(parsed.referenceId)
                        if (existingByRef != null) {
                            continue // Same transaction already recorded from another sender
                        }
                    }
                    
                    // Layer 3: Same amount + type within 5-minute window
                    // When bank and UPI app send SMS for same transaction, timestamps 
                    // are usually within seconds of each other
                    val timeWindowMs = 5 * 60 * 1000L // 5 minutes
                    val existingByAmountTime = repository.findDuplicateTransaction(
                        amount = parsed.amount,
                        transactionType = parsed.transactionType.name,
                        startTime = sms.date - timeWindowMs,
                        endTime = sms.date + timeWindowMs
                    )
                    if (existingByAmountTime != null) {
                        continue // Same transaction already recorded from another sender
                    }
                    
                    // ===== NOT A DUPLICATE — Insert new transaction =====
                    
                    // Find matching category based on rules
                    val categoryId = findCategoryForMerchant(parsed.merchant, rules)
                    
                    // Detect bank from sender
                    val bankInfo = com.hello.lets.test.data.entity.BankCodes.getBankFromSender(sms.address)
                    var bankAccountId: Long? = null
                    
                    if (bankInfo != null) {
                        val (bankCode, bankName) = bankInfo
                        
                        // Find existing account or create new one
                        var account = existingAccounts.find { it.bankCode == bankCode }
                        
                        if (account == null) {
                            // Create new bank account
                            val newAccount = com.hello.lets.test.data.entity.BankAccount(
                                bankName = bankName,
                                bankCode = bankCode,
                                accountNumber = parsed.accountNumber,
                                currentBalance = parsed.balance ?: 0.0,
                                isDefault = existingAccounts.isEmpty(),
                                colorHex = com.hello.lets.test.data.entity.BankCodes.getBankColor(bankCode)
                            )
                            val newId = database.bankAccountDao().insert(newAccount)
                            bankAccountId = newId
                        } else {
                            bankAccountId = account.id
                            
                            // Update balance if available in SMS
                            if (parsed.balance != null) {
                                database.bankAccountDao().updateBalance(account.id, parsed.balance)
                            }
                            
                            // Update account number if available and missing
                            if (account.accountNumber == null && parsed.accountNumber != null) {
                                val updatedAccount = account.copy(accountNumber = parsed.accountNumber)
                                database.bankAccountDao().update(updatedAccount)
                            }
                        }
                    }
                    
                    val transaction = Transaction(
                        amount = parsed.amount,
                        merchant = parsed.merchant ?: extractSenderName(sms.address),
                        categoryId = categoryId,
                        transactionType = parsed.transactionType,
                        transactionDate = sms.date,
                        rawSmsContent = sms.body,
                        smsAddress = sms.address,
                        referenceId = parsed.referenceId,
                        accountNumber = parsed.accountNumber,
                        balanceAfter = parsed.balance,
                        bankAccountId = bankAccountId
                    )
                    
                    val id = repository.insertTransaction(transaction)
                    if (id > 0) {
                        newTransactionCount++
                        
                        // ===== INTER-BANK TRANSFER DETECTION =====
                        // If user transfers from Bank A to Bank B:
                        //   Bank A sends "Rs.5000 debited" (DEBIT)
                        //   Bank B sends "Rs.5000 credited" (CREDIT)
                        // Both should exist for per-account tracking, but marked as TRANSFER
                        // so they don't inflate spending/income totals.
                        if (bankAccountId != null) {
                            val oppositeType = when (parsed.transactionType) {
                                TransactionType.DEBIT -> TransactionType.CREDIT.name
                                TransactionType.CREDIT -> TransactionType.DEBIT.name
                                else -> null
                            }
                            
                            if (oppositeType != null) {
                                val transferTimeWindowMs = 5 * 60 * 1000L // 5 minutes
                                val counterpart = repository.findTransferCounterpart(
                                    amount = parsed.amount,
                                    oppositeType = oppositeType,
                                    currentBankAccountId = bankAccountId,
                                    startTime = sms.date - transferTimeWindowMs,
                                    endTime = sms.date + transferTimeWindowMs
                                )
                                
                                if (counterpart != null) {
                                    // Found a matching counterpart — this is an inter-bank transfer!
                                    // Mark BOTH transactions as TRANSFER
                                    repository.updateTransactionType(id, TransactionType.TRANSFER.name)
                                    repository.updateTransactionType(counterpart.id, TransactionType.TRANSFER.name)
                                }
                            }
                        }
                    }
                }
                
                _syncState.update { 
                    it.copy(
                        isSyncing = false, 
                        syncedCount = newTransactionCount,
                        lastSyncTime = System.currentTimeMillis()
                    ) 
                }
                
            } catch (e: Exception) {
                _syncState.update { 
                    it.copy(
                        isSyncing = false, 
                        error = e.message ?: "Sync failed"
                    ) 
                }
            }
        }
    }
    
    /**
     * Find category ID for a merchant based on parsing rules.
     */
    private fun findCategoryForMerchant(
        merchant: String?,
        rules: List<com.hello.lets.test.data.entity.ParsingRule>
    ): Long? {
        if (merchant == null) return null
        
        val merchantLower = merchant.lowercase()
        
        // Find first matching rule (rules are sorted by priority)
        for (rule in rules) {
            if (merchantLower.contains(rule.keyword.lowercase())) {
                return rule.categoryId
            }
        }
        
        return null
    }
    
    /**
     * Extract a readable name from SMS sender address.
     */
    private fun extractSenderName(address: String): String {
        // Format: XX-HDFCBK -> HDFC Bank
        return address
            .replace(Regex("^[A-Z]{2}-"), "")
            .replace("BK", " Bank")
            .replace("BNK", " Bank")
            .trim()
            .ifEmpty { "Unknown" }
    }
    
    /**
     * Update a transaction (e.g., change category or notes).
     */
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }
    
    /**
     * Delete a transaction.
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}

/**
 * UI state for the Dashboard.
 */
data class DashboardUiState(
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val budget: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val transactionCount: Int = 0,
    val userName: String = "User",
    val bankAccounts: List<com.hello.lets.test.data.entity.BankAccount> = emptyList(),
    val isLoading: Boolean = true
) {
    val remainingBudget: Double get() = budget - totalSpent
    val spentPercentage: Float get() = if (budget > 0) (totalSpent / budget * 100).toFloat() else 0f
}

/**
 * State for SMS sync operation.
 */
data class SyncState(
    val isSyncing: Boolean = false,
    val syncedCount: Int = 0,
    val lastSyncTime: Long? = null,
    val error: String? = null
)

private data class StatsData(
    val spent: Double,
    val income: Double,
    val recentTransactions: List<Transaction>,
    val count: Int
)

private data class UserData(
    val profile: UserProfile?,
    val accounts: List<BankAccount>
)
