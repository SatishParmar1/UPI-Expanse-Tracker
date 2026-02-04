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
}

/**
 * Data class for category spending query result.
 */
data class CategorySpending(
    val categoryId: Long?,
    val total: Double
)
