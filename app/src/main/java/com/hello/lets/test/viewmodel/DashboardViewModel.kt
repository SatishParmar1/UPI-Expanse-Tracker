package com.hello.lets.test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.Category
import com.hello.lets.test.data.entity.Transaction
import com.hello.lets.test.data.entity.TransactionType
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
        database.excludedSenderDao()
    )
    private val smsReader = SmsReader(application)
    private val transactionParser = TransactionParser()
    
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
        repository.getTotalSpent(currentMonthStart, currentMonthEnd),
        repository.getTotalIncome(currentMonthStart, currentMonthEnd),
        repository.getRecentTransactions(5),
        repository.getTransactionCount()
    ) { spent, income, recentTransactions, count ->
        DashboardUiState(
            totalSpent = spent,
            totalIncome = income,
            recentTransactions = recentTransactions,
            transactionCount = count,
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
                
                var newTransactionCount = 0
                
                for (sms in messages) {
                    // Skip excluded senders
                    if (excludedAddresses.any { sms.address.contains(it, ignoreCase = true) }) {
                        continue
                    }
                    
                    // Check if already processed
                    val existing = repository.findTransactionByRawSms(sms.body)
                    if (existing != null) {
                        continue
                    }
                    
                    // Parse the SMS
                    val parsed = transactionParser.parse(sms.body)
                    
                    // Only save if we extracted valid data
                    if (parsed.amount != null && parsed.transactionType != null) {
                        // Find matching category based on rules
                        val categoryId = findCategoryForMerchant(parsed.merchant, rules)
                        
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
                            balanceAfter = parsed.balance
                        )
                        
                        val id = repository.insertTransaction(transaction)
                        if (id > 0) {
                            newTransactionCount++
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
    val budget: Double = 50000.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val transactionCount: Int = 0,
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
