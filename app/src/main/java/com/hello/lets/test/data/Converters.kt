package com.hello.lets.test.data

import androidx.room.TypeConverter
import com.hello.lets.test.data.entity.GoalPriority
import com.hello.lets.test.data.entity.TransactionType

/**
 * Type converters for Room database.
 */
class Converters {
    
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromGoalPriority(priority: GoalPriority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toGoalPriority(value: String): GoalPriority {
        return GoalPriority.valueOf(value)
    }
}
