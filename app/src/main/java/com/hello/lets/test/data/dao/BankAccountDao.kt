package com.hello.lets.test.data.dao

import androidx.room.*
import com.hello.lets.test.data.entity.BankAccount
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for BankAccount entity.
 */
@Dao
interface BankAccountDao {
    
    /**
     * Get all bank accounts as a Flow.
     */
    @Query("SELECT * FROM bank_accounts ORDER BY isDefault DESC, bankName ASC")
    fun getAllAccounts(): Flow<List<BankAccount>>
    
    /**
     * Get all bank accounts (non-Flow).
     */
    @Query("SELECT * FROM bank_accounts ORDER BY isDefault DESC, bankName ASC")
    suspend fun getAllAccountsList(): List<BankAccount>
    
    /**
     * Get a bank account by ID.
     */
    @Query("SELECT * FROM bank_accounts WHERE id = :id")
    suspend fun getById(id: Long): BankAccount?
    
    /**
     * Get a bank account by bank code.
     */
    @Query("SELECT * FROM bank_accounts WHERE bankCode = :bankCode LIMIT 1")
    suspend fun getByBankCode(bankCode: String): BankAccount?
    
    /**
     * Get the default bank account.
     */
    @Query("SELECT * FROM bank_accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(): BankAccount?
    
    /**
     * Get total balance across all accounts.
     */
    @Query("SELECT COALESCE(SUM(currentBalance), 0.0) FROM bank_accounts")
    fun getTotalBalance(): Flow<Double>
    
    /**
     * Get count of bank accounts.
     */
    @Query("SELECT COUNT(*) FROM bank_accounts")
    fun getAccountCount(): Flow<Int>
    
    /**
     * Insert a new bank account.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: BankAccount): Long
    
    /**
     * Insert multiple bank accounts.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<BankAccount>)
    
    /**
     * Update a bank account.
     */
    @Update
    suspend fun update(account: BankAccount)
    
    /**
     * Update balance for a specific account.
     */
    @Query("UPDATE bank_accounts SET currentBalance = :balance, lastUpdated = :timestamp WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, balance: Double, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update balance by bank code.
     */
    @Query("UPDATE bank_accounts SET currentBalance = :balance, lastUpdated = :timestamp WHERE bankCode = :bankCode")
    suspend fun updateBalanceByCode(bankCode: String, balance: Double, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Set an account as default (and unset others).
     */
    @Transaction
    suspend fun setAsDefault(accountId: Long) {
        clearDefault()
        markAsDefault(accountId)
    }
    
    @Query("UPDATE bank_accounts SET isDefault = 0")
    suspend fun clearDefault()
    
    @Query("UPDATE bank_accounts SET isDefault = 1 WHERE id = :accountId")
    suspend fun markAsDefault(accountId: Long)
    
    /**
     * Delete a bank account.
     */
    @Delete
    suspend fun delete(account: BankAccount)
    
    /**
     * Delete a bank account by ID.
     */
    @Query("DELETE FROM bank_accounts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
