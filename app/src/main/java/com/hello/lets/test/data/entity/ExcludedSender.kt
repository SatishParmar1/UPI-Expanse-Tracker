package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents SMS senders that should be excluded from transaction parsing.
 * Used to filter out promotional messages or non-transactional SMS.
 */
@Entity(tableName = "excluded_senders")
data class ExcludedSender(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** SMS sender address to exclude (e.g., "AD-PROMO") */
    val senderAddress: String,
    
    /** Optional reason for exclusion */
    val reason: String? = null
)
