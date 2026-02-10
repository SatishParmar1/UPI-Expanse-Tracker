package com.hello.lets.test.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hello.lets.test.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Transaction entity.
 */
@Dao
interface TransactionDao {
    
    /**
     * Get all transactions ordered by date (newest first).
     */
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    /**
     * Get transactions within a date range.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE transactionDate BETWEEN :startDate AND :endDate 
        ORDER BY transactionDate DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    
    /**
     * Get recent transactions with a limit.
     */
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    
    /**
     * Get a single transaction by ID.
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?
    
    /**
     * Get total amount spent (debited) in a date range.
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE transactionType = 'DEBIT' 
        AND transactionDate BETWEEN :startDate AND :endDate
    """)
    fun getTotalSpent(startDate: Long, endDate: Long): Flow<Double>
    
    /**
     * Get total income (credited) in a date range.
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE transactionType = 'CREDIT' 
        AND transactionDate BETWEEN :startDate AND :endDate
    """)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double>
    
    /**
     * Get total amount spent (debited) in a date range (Synchronous).
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE transactionType = 'DEBIT' 
        AND transactionDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalSpentSync(startDate: Long, endDate: Long): Double?
    
    /**
     * Get transactions by category.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE categoryId = :categoryId 
        ORDER BY transactionDate DESC
    """)
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    
    /**
     * Get spending summary by category for a date range.
     */
    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE transactionType = 'DEBIT' 
        AND transactionDate BETWEEN :startDate AND :endDate
        GROUP BY categoryId
    """)
    fun getSpendingByCategory(startDate: Long, endDate: Long): Flow<List<CategorySpending>>
    
    /**
     * Find transaction by raw SMS content (for duplicate detection).
     */
    @Query("SELECT * FROM transactions WHERE rawSmsContent = :rawSms LIMIT 1")
    suspend fun findByRawSms(rawSms: String): Transaction?
    
    /**
     * Find transaction by UPI/IMPS/NEFT reference ID (for cross-sender duplicate detection).
     * If two SMSes (bank + UPI app) share the same reference ID, they are the same transaction.
     */
    @Query("SELECT * FROM transactions WHERE referenceId = :referenceId AND referenceId IS NOT NULL LIMIT 1")
    suspend fun findByReferenceId(referenceId: String): Transaction?
    
    /**
     * Find a potential duplicate transaction by matching amount, type, and a close time window.
     * Used when reference ID is not available — if the same amount + type occurs within
     * a few minutes, it's almost certainly the same transaction from a different sender.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE amount = :amount 
        AND transactionType = :transactionType 
        AND transactionDate BETWEEN :startTime AND :endTime 
        LIMIT 1
    """)
    suspend fun findDuplicate(
        amount: Double, 
        transactionType: String, 
        startTime: Long, 
        endTime: Long
    ): Transaction?
    
    /**
     * Insert a new transaction.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction): Long
    
    /**
     * Insert multiple transactions.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>): List<Long>
    
    /**
     * Update an existing transaction.
     */
    @Update
    suspend fun update(transaction: Transaction)
    
    /**
     * Delete a transaction.
     */
    @Delete
    suspend fun delete(transaction: Transaction)
    
    /**
     * Delete all transactions.
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
    
    /**
     * Get transaction count.
     */
    @Query("SELECT COUNT(*) FROM transactions")
    fun getCount(): Flow<Int>
    
    /**
     * Find a potential inter-bank transfer counterpart.
     * When user transfers from Bank A to Bank B, Bank A sends DEBIT SMS and Bank B sends CREDIT SMS.
     * This finds the opposite-type transaction with the same amount from a DIFFERENT bank account
     * within a time window.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE amount = :amount 
        AND transactionType = :oppositeType 
        AND bankAccountId != :currentBankAccountId 
        AND bankAccountId IS NOT NULL
        AND transactionDate BETWEEN :startTime AND :endTime 
        LIMIT 1
    """)
    suspend fun findTransferCounterpart(
        amount: Double,
        oppositeType: String,
        currentBankAccountId: Long,
        startTime: Long,
        endTime: Long
    ): Transaction?
    
    /**
     * Update the transaction type (e.g., mark as TRANSFER).
     */
    @Query("UPDATE transactions SET transactionType = :newType WHERE id = :transactionId")
    suspend fun updateTransactionType(transactionId: Long, newType: String)

    /**
     * Get transactions for a specific bank account.
     */
    @Query("SELECT * FROM transactions WHERE bankAccountId = :bankAccountId ORDER BY transactionDate DESC")
    fun getTransactionsByBankAccount(bankAccountId: Long): Flow<List<Transaction>>
}

/**
 * Data class for category spending query result.
 */
data class CategorySpending(
    val categoryId: Long?,
    val total: Double
)
