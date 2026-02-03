package com.hello.lets.test.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hello.lets.test.data.entity.ParsingRule
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ParsingRule entity.
 */
@Dao
interface ParsingRuleDao {
    
    /**
     * Get all parsing rules ordered by priority.
     */
    @Query("SELECT * FROM parsing_rules ORDER BY priority DESC, keyword ASC")
    fun getAllRules(): Flow<List<ParsingRule>>
    
    /**
     * Get only active parsing rules.
     */
    @Query("SELECT * FROM parsing_rules WHERE isActive = 1 ORDER BY priority DESC")
    fun getActiveRules(): Flow<List<ParsingRule>>
    
    /**
     * Get active rules as a list (non-Flow for sync operations).
     */
    @Query("SELECT * FROM parsing_rules WHERE isActive = 1 ORDER BY priority DESC")
    suspend fun getActiveRulesList(): List<ParsingRule>
    
    /**
     * Get rules by category.
     */
    @Query("SELECT * FROM parsing_rules WHERE categoryId = :categoryId")
    fun getRulesByCategory(categoryId: Long): Flow<List<ParsingRule>>
    
    /**
     * Find rule by keyword.
     */
    @Query("SELECT * FROM parsing_rules WHERE keyword = :keyword LIMIT 1")
    suspend fun findByKeyword(keyword: String): ParsingRule?
    
    /**
     * Insert a new parsing rule.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ParsingRule): Long
    
    /**
     * Insert multiple rules.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rules: List<ParsingRule>): List<Long>
    
    /**
     * Update a parsing rule.
     */
    @Update
    suspend fun update(rule: ParsingRule)
    
    /**
     * Delete a parsing rule.
     */
    @Delete
    suspend fun delete(rule: ParsingRule)
    
    /**
     * Get rule count.
     */
    @Query("SELECT COUNT(*) FROM parsing_rules WHERE isActive = 1")
    fun getActiveCount(): Flow<Int>
}
