package com.hello.lets.test.data.dao

import androidx.room.*
import com.hello.lets.test.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for UserProfile entity.
 */
@Dao
interface UserProfileDao {
    
    /**
     * Get the user profile as a Flow.
     */
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>
    
    /**
     * Get the user profile (non-Flow).
     */
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileSync(): UserProfile?
    
    /**
     * Check if profile exists.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM user_profile WHERE id = 1)")
    suspend fun hasProfile(): Boolean
    
    /**
     * Check if onboarding is complete.
     */
    @Query("SELECT onboardingComplete FROM user_profile WHERE id = 1")
    suspend fun isOnboardingComplete(): Boolean?
    
    /**
     * Insert or update user profile.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)
    
    /**
     * Update user profile.
     */
    @Update
    suspend fun update(profile: UserProfile)
    
    /**
     * Update just the name.
     */
    @Query("UPDATE user_profile SET name = :name, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateName(name: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update phone number.
     */
    @Query("UPDATE user_profile SET phoneNumber = :phone, updatedAt = :timestamp WHERE id = 1")
    suspend fun updatePhone(phone: String?, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Mark onboarding as complete.
     */
    @Query("UPDATE user_profile SET onboardingComplete = 1, updatedAt = :timestamp WHERE id = 1")
    suspend fun completeOnboarding(timestamp: Long = System.currentTimeMillis())
    
    /**
     * Delete user profile.
     */
    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
}
