package com.hello.lets.test.data.dao

import androidx.room.*
import com.hello.lets.test.data.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE isActive = 1")
    fun getActiveBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)
    
    @Query("SELECT COUNT(*) FROM budgets WHERE isActive = 1")
    fun getActiveCount(): Flow<Int>
}
