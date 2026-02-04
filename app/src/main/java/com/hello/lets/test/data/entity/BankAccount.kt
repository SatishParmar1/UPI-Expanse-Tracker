package com.hello.lets.test.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a bank account linked to the user.
 * Used for multi-bank transaction tracking.
 */
@Entity(
    tableName = "bank_accounts",
    indices = [
        Index(value = ["bankCode"], unique = true)
    ]
)
data class BankAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Full bank name (e.g., "HDFC Bank", "State Bank of India") */
    val bankName: String,
    
    /** Short bank code from SMS sender (e.g., "HDFC", "SBI", "ICICI") */
    val bankCode: String,
    
    /** Last 4 digits of account number (e.g., "1234") */
    val accountNumber: String? = null,
    
    /** Current balance (updated from SMS) */
    val currentBalance: Double = 0.0,
    
    /** Last time balance was updated */
    val lastUpdated: Long = System.currentTimeMillis(),
    
    /** Whether this is the default/primary account */
    val isDefault: Boolean = false,
    
    /** Color for UI display (hex format) */
    val colorHex: String = "#1A73E8",
    
    /** Icon name for the bank */
    val iconName: String? = null,
    
    /** Record creation timestamp */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Common Indian bank codes mapped from SMS sender addresses.
 */
object BankCodes {
    private val DEFAULT_BANK_MAP = mapOf(
        // HDFC Bank
        "HDFCBK" to Pair("HDFC", "HDFC Bank"),
        "HDFCB" to Pair("HDFC", "HDFC Bank"),
        
        // State Bank of India
        "SBIINB" to Pair("SBI", "State Bank of India"),
        "SBIPSG" to Pair("SBI", "State Bank of India"),
        "SBMSMS" to Pair("SBI", "State Bank of India"),
        
        // ICICI Bank
        "ICICIB" to Pair("ICICI", "ICICI Bank"),
        "ICICBA" to Pair("ICICI", "ICICI Bank"),
        
        // Axis Bank
        "AXISBK" to Pair("AXIS", "Axis Bank"),
        "AXISB" to Pair("AXIS", "Axis Bank"),
        
        // Kotak Mahindra Bank
        "KOTAKB" to Pair("KOTAK", "Kotak Mahindra Bank"),
        
        // Yes Bank
        "YESBNK" to Pair("YES", "Yes Bank"),
        
        // Punjab National Bank
        "PNBSMS" to Pair("PNB", "Punjab National Bank"),
        
        // Bank of Baroda
        "BOBTXT" to Pair("BOB", "Bank of Baroda"),
        
        // Canara Bank
        "CANBNK" to Pair("CANARA", "Canara Bank"),
        
        // IDFC First Bank
        "IDFCFB" to Pair("IDFC", "IDFC First Bank"),
        
        // IndusInd Bank
        "INDUSB" to Pair("INDUS", "IndusInd Bank"),
        
        // Federal Bank
        "FEDBK" to Pair("FEDERAL", "Federal Bank"),
        
        // RBL Bank
        "RBLBNK" to Pair("RBL", "RBL Bank"),
        
        // PayTM Payments Bank
        "PYTM" to Pair("PAYTM", "Paytm Payments Bank"),
        "PAYTMB" to Pair("PAYTM", "Paytm Payments Bank"),
        
        // PhonePe (not a bank but used for UPI)
        "PHONPE" to Pair("PHONEPE", "PhonePe"),
        
        // Google Pay
        "GPAY" to Pair("GPAY", "Google Pay"),
        
        // Union Bank
        "UBOI" to Pair("UNION", "Union Bank of India")
    )
    
    // Mutable map to allow user to add custom banks
    // Maps sender ID (e.g. HDFCBK) to Pair(Code, Name)
    private val customBankMap = mutableMapOf<String, Pair<String, String>>()
    
    /**
     * Add a custom bank mapping.
     * @param sender The SMS sender ID (e.g. "MYBANK")
     * @param code Short code (e.g. "MYB")
     * @param name Full bank name
     */
    fun addCustomBank(sender: String, code: String, name: String) {
        customBankMap[sender.uppercase()] = Pair(code, name)
    }
    
    /**
     * Extract bank code and name from SMS sender address.
     * @param smsAddress The sender address (e.g., "VM-HDFCBK", "AD-SBIPSG")
     * @return Pair of (bankCode, bankName) or null if not recognized
     */
    fun getBankFromSender(smsAddress: String): Pair<String, String>? {
        // Remove common prefixes like VM-, AD-, AX-, BZ-, JD-
        val cleaned = smsAddress.uppercase()
            .replace(Regex("^(VM-|AD-|AX-|BZ-|JD-|VK-|BW-)"), "")
            .replace(Regex("^[A-Z]{2}-"), "") // Handle generic 2-letter prefixes
        
        // Check custom map first
        customBankMap[cleaned]?.let { return it }
        
        // Try exact match in default map
        DEFAULT_BANK_MAP[cleaned]?.let { return it }
        
        // Try partial match
        for ((key, value) in customBankMap) {
            if (cleaned.contains(key) || key.contains(cleaned)) {
                return value
            }
        }
        
        for ((key, value) in DEFAULT_BANK_MAP) {
            if (cleaned.contains(key) || key.contains(cleaned)) {
                return value
            }
        }
        
        return null
    }
    
    /**
     * Get default color for a bank.
     */
    fun getBankColor(bankCode: String): String {
        return when (bankCode) {
            "HDFC" -> "#004C8F"
            "SBI" -> "#1a4480"
            "ICICI" -> "#B02A30"
            "AXIS" -> "#97144D"
            "KOTAK" -> "#ED1C24"
            "YES" -> "#00518F"
            "PNB" -> "#E42127"
            "BOB" -> "#F47920"
            "CANARA" -> "#FFCB05"
            "IDFC" -> "#9C1D26"
            "INDUS" -> "#98272A"
            "FEDERAL" -> "#00529B"
            "RBL" -> "#21409A"
            "PAYTM" -> "#00B9F5"
            "PHONEPE" -> "#5F259F"
            "GPAY" -> "#4285F4"
            else -> "#1A73E8"
        }
    }
}
