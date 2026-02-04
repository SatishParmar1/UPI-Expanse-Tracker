package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the user's profile information.
 * Single record table (only one user).
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Long = 1,  // Single user, always ID 1
    
    /** User's display name */
    val name: String,
    
    /** User's phone number */
    val phoneNumber: String? = null,
    
    /** User's email (optional) */
    val email: String? = null,
    
    /** Profile picture path (optional) */
    val profilePicturePath: String? = null,
    
    /** Whether onboarding is complete */
    val onboardingComplete: Boolean = false,
    
    /** Record creation timestamp */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Last updated timestamp */
    val updatedAt: Long = System.currentTimeMillis()
)
