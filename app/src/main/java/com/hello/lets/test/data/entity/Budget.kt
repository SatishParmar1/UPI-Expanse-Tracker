package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = BankAccount::class,
            parentColumns = ["id"],
            childColumns = ["bankAccountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "", // User-friendly name like "Food Budget", "Monthly Savings"
    val amount: Double,
    val type: BudgetType, // DAILY, WEEKLY or MONTHLY
    val categoryId: Long? = null, // null means global budget (all categories)
    val bankAccountId: Long? = null, // null means all accounts, specific ID for account-specific budget
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val startDate: Long = System.currentTimeMillis() // When this budget period started
)
