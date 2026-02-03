package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Rules for automatically categorizing transactions based on keywords.
 * When a keyword is found in a transaction's merchant name, 
 * the transaction is assigned the associated category.
 */
@Entity(
    tableName = "parsing_rules",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class ParsingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Keyword to match in merchant name (e.g., "Swiggy", "Uber") */
    val keyword: String,
    
    /** Category to assign when keyword matches */
    val categoryId: Long? = null,
    
    /** Whether this rule is currently active */
    val isActive: Boolean = true,
    
    /** Priority for rule matching (higher = checked first) */
    val priority: Int = 0
)
