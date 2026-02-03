package com.hello.lets.test.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hello.lets.test.data.entity.ExcludedSender
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ExcludedSender entity.
 */
@Dao
interface ExcludedSenderDao {
    
    /**
     * Get all excluded senders.
     */
    @Query("SELECT * FROM excluded_senders ORDER BY senderAddress ASC")
    fun getAllExcludedSenders(): Flow<List<ExcludedSender>>
    
    /**
     * Get list of excluded sender addresses for filtering.
     */
    @Query("SELECT senderAddress FROM excluded_senders")
    suspend fun getExcludedAddresses(): List<String>
    
    /**
     * Check if a sender is excluded.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM excluded_senders WHERE senderAddress = :address)")
    suspend fun isExcluded(address: String): Boolean
    
    /**
     * Insert an excluded sender.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sender: ExcludedSender): Long
    
    /**
     * Delete an excluded sender.
     */
    @Delete
    suspend fun delete(sender: ExcludedSender)
    
    /**
     * Get excluded sender count.
     */
    @Query("SELECT COUNT(*) FROM excluded_senders")
    fun getCount(): Flow<Int>
}
