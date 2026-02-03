package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a transaction category for organizing expenses.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Category display name (e.g., "Food & Dining") */
    val name: String,
    
    /** Material icon name for display */
    val iconName: String,
    
    /** Color in hex format (e.g., "#FF5722") */
    val colorHex: String,
    
    /** Whether this is a system default category */
    val isDefault: Boolean = false
)
