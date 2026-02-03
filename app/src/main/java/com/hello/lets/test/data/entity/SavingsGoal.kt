package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a savings goal with target amount and progress tracking.
 */
@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Goal name (e.g., "Emergency Fund", "Europe Trip") */
    val name: String,
    
    /** Goal description/subtitle */
    val description: String? = null,
    
    /** Target amount to save */
    val targetAmount: Double,
    
    /** Current saved amount */
    val savedAmount: Double = 0.0,
    
    /** Priority level: HIGH, MEDIUM, LOW */
    val priority: GoalPriority = GoalPriority.MEDIUM,
    
    /** Icon identifier for the goal */
    val iconName: String = "savings",
    
    /** Color in hex format */
    val colorHex: String = "#4CAF50",
    
    /** Whether Smart Save (auto-save) is enabled */
    val smartSaveEnabled: Boolean = false,
    
    /** Target date for goal completion (optional) */
    val targetDate: Long? = null,
    
    /** Record creation timestamp */
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Float get() = 
        if (targetAmount > 0) ((savedAmount / targetAmount) * 100).toFloat().coerceIn(0f, 100f) else 0f
    
    val isCompleted: Boolean get() = savedAmount >= targetAmount
}

enum class GoalPriority {
    HIGH,
    MEDIUM,
    LOW
}
