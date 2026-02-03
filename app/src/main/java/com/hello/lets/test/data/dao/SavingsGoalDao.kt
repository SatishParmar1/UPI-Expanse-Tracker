package com.hello.lets.test.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hello.lets.test.data.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SavingsGoal entity.
 */
@Dao
interface SavingsGoalDao {
    
    /**
     * Get all savings goals ordered by priority and creation date.
     */
    @Query("SELECT * FROM savings_goals ORDER BY priority ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>
    
    /**
     * Get total saved amount across all goals.
     */
    @Query("SELECT COALESCE(SUM(savedAmount), 0.0) FROM savings_goals")
    fun getTotalSaved(): Flow<Double>
    
    /**
     * Get total target amount across all goals.
     */
    @Query("SELECT COALESCE(SUM(targetAmount), 0.0) FROM savings_goals")
    fun getTotalTarget(): Flow<Double>
    
    /**
     * Get goals with Smart Save enabled.
     */
    @Query("SELECT * FROM savings_goals WHERE smartSaveEnabled = 1")
    fun getSmartSaveGoals(): Flow<List<SavingsGoal>>
    
    /**
     * Get goal by ID.
     */
    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getById(id: Long): SavingsGoal?
    
    /**
     * Insert a new goal.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal): Long
    
    /**
     * Update a goal.
     */
    @Update
    suspend fun update(goal: SavingsGoal)
    
    /**
     * Delete a goal.
     */
    @Delete
    suspend fun delete(goal: SavingsGoal)
    
    /**
     * Get goal count.
     */
    @Query("SELECT COUNT(*) FROM savings_goals")
    fun getCount(): Flow<Int>
}
