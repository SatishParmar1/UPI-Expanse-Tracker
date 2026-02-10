package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a financial transaction extracted from SMS messages.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["transactionDate"]),
        Index(value = ["rawSmsContent"], unique = true)
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Transaction amount in INR */
    val amount: Double,
    
    /** Merchant/Vendor name (e.g., "Swiggy", "Amazon") */
    val merchant: String,
    
    /** Foreign key to Category table */
    val categoryId: Long? = null,
    
    /** Type of transaction: DEBIT or CREDIT */
    val transactionType: TransactionType,
    
    /** Transaction timestamp in milliseconds */
    val transactionDate: Long,
    
    /** Original SMS text for reference */
    val rawSmsContent: String,
    
    /** SMS sender address (e.g., "AX-HDFCBK", "VM-SBIINB") */
    val smsAddress: String,
    
    /** UPI/IMPS/NEFT reference ID */
    val referenceId: String? = null,
    
    /** Last 4 digits of account number (e.g., "XX89") */
    val accountNumber: String? = null,
    
    /** Balance after transaction */
    val balanceAfter: Double? = null,
    
    /** Foreign key to BankAccount (for multi-bank support) */
    val bankAccountId: Long? = null,
    
    /** User-added notes */
    val notes: String? = null,
    
    /** Flag indicating local processing */
    val isProcessedLocally: Boolean = true,
    
    /** Record creation timestamp */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Type of financial transaction.
 */
enum class TransactionType {
    DEBIT,    // Money spent/debited
    CREDIT,   // Money received/credited
    TRANSFER  // Money transferred between own accounts (not counted in spending/income)
}
