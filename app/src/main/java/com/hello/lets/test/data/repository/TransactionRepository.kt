package com.hello.lets.test.data.repository

import com.hello.lets.test.data.dao.CategoryDao
import com.hello.lets.test.data.dao.ExcludedSenderDao
import com.hello.lets.test.data.dao.ParsingRuleDao
import com.hello.lets.test.data.dao.TransactionDao
import com.hello.lets.test.data.entity.Category
import com.hello.lets.test.data.entity.ExcludedSender
import com.hello.lets.test.data.entity.ParsingRule
import com.hello.lets.test.data.entity.Transaction
import com.hello.lets.test.data.dao.BudgetDao
import com.hello.lets.test.data.entity.Budget
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing all database operations.
 * Acts as a single source of truth for the data layer.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val parsingRuleDao: ParsingRuleDao,
    private val excludedSenderDao: ExcludedSenderDao,
    private val budgetDao: BudgetDao
) {
    
    // ==================== Transactions ====================
    
    fun getAllTransactions(): Flow<List<Transaction>> = 
        transactionDao.getAllTransactions()
    
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>> = 
        transactionDao.getRecentTransactions(limit)
    
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)
    
    fun getTotalSpent(startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getTotalSpent(startDate, endDate)
    
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getTotalIncome(startDate, endDate)
    
    fun getTransactionCount(): Flow<Int> = 
        transactionDao.getCount()
    
    suspend fun findTransactionByRawSms(rawSms: String): Transaction? =
        transactionDao.findByRawSms(rawSms)
    
    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insert(transaction)
    
    suspend fun insertTransactions(transactions: List<Transaction>): List<Long> =
        transactionDao.insertAll(transactions)
    
    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction)
    
    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.delete(transaction)
    
    // ==================== Categories ====================
    
    fun getAllCategories(): Flow<List<Category>> = 
        categoryDao.getAllCategories()
    
    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getById(id)
    
    suspend fun getCategoryByName(name: String): Category? =
        categoryDao.getByName(name)
    
    suspend fun insertCategory(category: Category): Long =
        categoryDao.insert(category)
    
    // ==================== Parsing Rules ====================
    
    fun getAllParsingRules(): Flow<List<ParsingRule>> =
        parsingRuleDao.getAllRules()
    
    fun getActiveParsingRules(): Flow<List<ParsingRule>> =
        parsingRuleDao.getActiveRules()
    
    suspend fun getActiveRulesList(): List<ParsingRule> =
        parsingRuleDao.getActiveRulesList()
    
    fun getActiveRulesCount(): Flow<Int> =
        parsingRuleDao.getActiveCount()
    
    suspend fun insertParsingRule(rule: ParsingRule): Long =
        parsingRuleDao.insert(rule)
    
    suspend fun updateParsingRule(rule: ParsingRule) =
        parsingRuleDao.update(rule)
    
    suspend fun deleteParsingRule(rule: ParsingRule) =
        parsingRuleDao.delete(rule)
    
    // ==================== Excluded Senders ====================
    
    fun getAllExcludedSenders(): Flow<List<ExcludedSender>> =
        excludedSenderDao.getAllExcludedSenders()
    
    suspend fun getExcludedAddresses(): List<String> =
        excludedSenderDao.getExcludedAddresses()
    
    suspend fun isAddressExcluded(address: String): Boolean =
        excludedSenderDao.isExcluded(address)
    
    fun getExcludedSenderCount(): Flow<Int> =
        excludedSenderDao.getCount()
    
    suspend fun insertExcludedSender(sender: ExcludedSender): Long =
        excludedSenderDao.insert(sender)
    
    suspend fun deleteExcludedSender(sender: ExcludedSender) =
        excludedSenderDao.delete(sender)

    // ==================== Budgets ====================

    fun getAllBudgets(): Flow<List<Budget>> =
        budgetDao.getAllBudgets()

    fun getActiveBudgets(): Flow<List<Budget>> =
        budgetDao.getActiveBudgets()

    fun getActiveBudgetCount(): Flow<Int> =
        budgetDao.getActiveCount()

    suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insert(budget)

    suspend fun updateBudget(budget: Budget) =
        budgetDao.update(budget)

    suspend fun deleteBudget(budget: Budget) =
        budgetDao.delete(budget)
}
