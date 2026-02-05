package com.hello.lets.test.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hello.lets.test.data.dao.BankAccountDao
import com.hello.lets.test.data.dao.CategoryDao
import com.hello.lets.test.data.dao.ExcludedSenderDao
import com.hello.lets.test.data.dao.ParsingRuleDao
import com.hello.lets.test.data.dao.SavingsGoalDao
import com.hello.lets.test.data.dao.TransactionDao
import com.hello.lets.test.data.dao.UserProfileDao
import com.hello.lets.test.data.entity.BankAccount
import com.hello.lets.test.data.entity.Category
import com.hello.lets.test.data.entity.ExcludedSender
import com.hello.lets.test.data.entity.ParsingRule
import com.hello.lets.test.data.entity.SavingsGoal
import com.hello.lets.test.data.entity.Transaction
import com.hello.lets.test.data.entity.Budget
import com.hello.lets.test.data.entity.UserProfile
import com.hello.lets.test.data.dao.BudgetDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room database for the Payment Tracker app.
 */
@Database(
    entities = [
        Transaction::class,
        BankAccount::class,
        SavingsGoal::class,
        Category::class,
        ParsingRule::class,
        ExcludedSender::class,
        Budget::class,
        UserProfile::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun categoryDao(): CategoryDao
    abstract fun parsingRuleDao(): ParsingRuleDao
    abstract fun excludedSenderDao(): ExcludedSenderDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userProfileDao(): UserProfileDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 1 to 2: Add bank_accounts and other tables if needed
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Since we are adding Budget table, we need to create it.
                // Note: The user mentioned "Fallback to destructive" in plan but code has MIGRATION_1_2.
                // Let's implement proper migration for Budget.
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `categoryId` INTEGER, 
                        `isActive` INTEGER NOT NULL DEFAULT 1, 
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Also ensure other tables from previous migration are present if this runs on fresh v1
                // But simplified: usually we just adding Budget now.
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "payment_tracker_db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Callback to populate database with default data on creation.
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultData(database)
                }
            }
        }
        
        private suspend fun populateDefaultData(database: AppDatabase) {
            val categoryDao = database.categoryDao()
            val parsingRuleDao = database.parsingRuleDao()
            
            // Insert default categories
            val categories = listOf(
                Category(id = 1, name = "Food & Dining", iconName = "restaurant", colorHex = "#FF5722", isDefault = true),
                Category(id = 2, name = "Transport", iconName = "directions_car", colorHex = "#2196F3", isDefault = true),
                Category(id = 3, name = "Entertainment", iconName = "movie", colorHex = "#9C27B0", isDefault = true),
                Category(id = 4, name = "Groceries", iconName = "shopping_cart", colorHex = "#4CAF50", isDefault = true),
                Category(id = 5, name = "Shopping", iconName = "shopping_bag", colorHex = "#E91E63", isDefault = true),
                Category(id = 6, name = "Bills & Utilities", iconName = "receipt", colorHex = "#607D8B", isDefault = true),
                Category(id = 7, name = "Income", iconName = "attach_money", colorHex = "#4CAF50", isDefault = true),
                Category(id = 8, name = "Health", iconName = "local_hospital", colorHex = "#F44336", isDefault = true),
                Category(id = 9, name = "Education", iconName = "school", colorHex = "#3F51B5", isDefault = true),
                Category(id = 10, name = "Other", iconName = "more_horiz", colorHex = "#9E9E9E", isDefault = true)
            )
            categoryDao.insertAll(categories)
            
            // Insert default parsing rules
            val rules = listOf(
                // Food & Dining
                ParsingRule(keyword = "Swiggy", categoryId = 1, priority = 10),
                ParsingRule(keyword = "Zomato", categoryId = 1, priority = 10),
                ParsingRule(keyword = "Dominos", categoryId = 1, priority = 10),
                ParsingRule(keyword = "McDonald", categoryId = 1, priority = 10),
                ParsingRule(keyword = "KFC", categoryId = 1, priority = 10),
                ParsingRule(keyword = "Pizza Hut", categoryId = 1, priority = 10),
                ParsingRule(keyword = "Starbucks", categoryId = 1, priority = 10),
                
                // Transport
                ParsingRule(keyword = "Uber", categoryId = 2, priority = 10),
                ParsingRule(keyword = "Ola", categoryId = 2, priority = 10),
                ParsingRule(keyword = "Rapido", categoryId = 2, priority = 10),
                ParsingRule(keyword = "Metro", categoryId = 2, priority = 5),
                ParsingRule(keyword = "IRCTC", categoryId = 2, priority = 10),
                ParsingRule(keyword = "Petrol", categoryId = 2, priority = 8),
                ParsingRule(keyword = "HP Fuel", categoryId = 2, priority = 10),
                ParsingRule(keyword = "Indian Oil", categoryId = 2, priority = 10),
                
                // Entertainment
                ParsingRule(keyword = "Netflix", categoryId = 3, priority = 10),
                ParsingRule(keyword = "Spotify", categoryId = 3, priority = 10),
                ParsingRule(keyword = "Prime Video", categoryId = 3, priority = 10),
                ParsingRule(keyword = "Hotstar", categoryId = 3, priority = 10),
                ParsingRule(keyword = "YouTube", categoryId = 3, priority = 8),
                ParsingRule(keyword = "PVR", categoryId = 3, priority = 10),
                ParsingRule(keyword = "INOX", categoryId = 3, priority = 10),
                ParsingRule(keyword = "BookMyShow", categoryId = 3, priority = 10),
                
                // Groceries
                ParsingRule(keyword = "Blinkit", categoryId = 4, priority = 10),
                ParsingRule(keyword = "Zepto", categoryId = 4, priority = 10),
                ParsingRule(keyword = "BigBasket", categoryId = 4, priority = 10),
                ParsingRule(keyword = "DMart", categoryId = 4, priority = 10),
                ParsingRule(keyword = "Reliance Fresh", categoryId = 4, priority = 10),
                ParsingRule(keyword = "More Supermarket", categoryId = 4, priority = 10),
                
                // Shopping
                ParsingRule(keyword = "Amazon", categoryId = 5, priority = 10),
                ParsingRule(keyword = "Flipkart", categoryId = 5, priority = 10),
                ParsingRule(keyword = "Myntra", categoryId = 5, priority = 10),
                ParsingRule(keyword = "Ajio", categoryId = 5, priority = 10),
                ParsingRule(keyword = "Nykaa", categoryId = 5, priority = 10),
                ParsingRule(keyword = "Meesho", categoryId = 5, priority = 10),
                
                // Bills & Utilities
                ParsingRule(keyword = "Electricity", categoryId = 6, priority = 8),
                ParsingRule(keyword = "BESCOM", categoryId = 6, priority = 10),
                ParsingRule(keyword = "Gas Bill", categoryId = 6, priority = 8),
                ParsingRule(keyword = "Airtel", categoryId = 6, priority = 10),
                ParsingRule(keyword = "Jio", categoryId = 6, priority = 10),
                ParsingRule(keyword = "Vodafone", categoryId = 6, priority = 10),
                ParsingRule(keyword = "ACT Fibernet", categoryId = 6, priority = 10),
                
                // Income keywords
                ParsingRule(keyword = "Salary", categoryId = 7, priority = 10),
                ParsingRule(keyword = "credited", categoryId = 7, priority = 5),
                ParsingRule(keyword = "Refund", categoryId = 7, priority = 8),
                
                // Health
                ParsingRule(keyword = "Apollo", categoryId = 8, priority = 10),
                ParsingRule(keyword = "Pharmacy", categoryId = 8, priority = 8),
                ParsingRule(keyword = "PharmEasy", categoryId = 8, priority = 10),
                ParsingRule(keyword = "1mg", categoryId = 8, priority = 10),
                ParsingRule(keyword = "Netmeds", categoryId = 8, priority = 10)
            )
            parsingRuleDao.insertAll(rules)
        }
    }
}
