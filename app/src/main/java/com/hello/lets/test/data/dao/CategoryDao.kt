package com.hello.lets.test.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hello.lets.test.data.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Category entity.
 */
@Dao
interface CategoryDao {
    
    /**
     * Get all categories.
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    /**
     * Get default (system) categories.
     */
    @Query("SELECT * FROM categories WHERE isDefault = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<Category>>
    
    /**
     * Get category by ID.
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?
    
    /**
     * Get category by name.
     */
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Category?
    
    /**
     * Insert a new category.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long
    
    /**
     * Insert multiple categories.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>): List<Long>
    
    /**
     * Update a category.
     */
    @Update
    suspend fun update(category: Category)
    
    /**
     * Delete a category.
     */
    @Delete
    suspend fun delete(category: Category)
    
    /**
     * Get category count.
     */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}
