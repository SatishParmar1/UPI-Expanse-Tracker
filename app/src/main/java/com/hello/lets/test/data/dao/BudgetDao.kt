package com.hello.lets.test.data.dao

import androidx.room.*
import com.hello.lets.test.data.entity.Budget
import com.hello.lets.test.data.entity.BudgetType
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE isActive = 1")
    fun getActiveBudgets(): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE isActive = 1")
    suspend fun getActiveBudgetsSync(): List<Budget>
    
    @Query("SELECT * FROM budgets WHERE type = :type AND isActive = 1")
    fun getBudgetsByType(type: BudgetType): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE bankAccountId = :accountId AND isActive = 1")
    fun getBudgetsByAccount(accountId: Long): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE type = :type AND bankAccountId = :accountId AND isActive = 1")
    fun getBudgetsByTypeAndAccount(type: BudgetType, accountId: Long): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE (bankAccountId = :accountId OR bankAccountId IS NULL) AND isActive = 1")
    fun getBudgetsForAccount(accountId: Long?): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)
    
    @Query("SELECT COUNT(*) FROM budgets WHERE isActive = 1")
    fun getActiveCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM budgets WHERE type = :type AND isActive = 1")
    fun getCountByType(type: BudgetType): Flow<Int>
}
