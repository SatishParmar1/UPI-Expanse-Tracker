package com.hello.lets.test.sms

import android.content.Context
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads SMS messages from the device's SMS inbox.
 * Requires READ_SMS permission.
 */
class SmsReader(private val context: Context) {
    
    /**
     * Represents a single SMS message.
     */
    data class SmsMessage(
        val id: Long,
        /** Sender address (e.g., "AX-HDFCBK", "VM-SBIINB") */
        val address: String,
        /** SMS body content */
        val body: String,
        /** Timestamp when SMS was received */
        val date: Long,
        /** Whether the SMS has been read */
        val isRead: Boolean
    )
    
    /**
     * Bank SMS sender patterns - typically in format XX-BANKNAME or XY-BANKABB
     */
    private val bankSenderPatterns = listOf(
        Regex("^[A-Z]{2}-[A-Z]{2,}$"),           // XX-HDFCBK, AX-ICICI
        Regex("^[A-Z]{2}-[A-Z0-9]{4,}$"),        // VM-SBIINB
        Regex("^[A-Z]{2}-[A-Z]{3,}BK$"),         // XX-HDFCBK
        Regex("^[A-Z]{2}-[A-Z]{3,}BNK$"),        // XX-ICICBNK
        Regex(".*BANK.*", RegexOption.IGNORE_CASE), // Contains BANK
        Regex(".*UPI.*", RegexOption.IGNORE_CASE),  // Contains UPI
    )
    
    /**
     * Read all SMS messages from inbox.
     * @param limit Maximum number of messages to retrieve
     * @return List of SMS messages, newest first
     */
    suspend fun readAllSms(limit: Int = 500): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()
        
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.READ
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            
            cursor?.use {
                var count = 0
                while (it.moveToNext() && count < limit) {
                    val address = it.getString(1) ?: ""
                    val body = it.getString(2) ?: ""
                    
                    messages.add(
                        SmsMessage(
                            id = it.getLong(0),
                            address = address,
                            body = body,
                            date = it.getLong(3),
                            isRead = it.getInt(4) == 1
                        )
                    )
                    count++
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        messages
    }
    
    /**
     * Read only bank/financial SMS messages.
     * Filters messages based on sender patterns and content keywords.
     */
    suspend fun readBankSms(limit: Int = 500): List<SmsMessage> = withContext(Dispatchers.IO) {
        val allMessages = readAllSms(limit * 2) // Read more to filter
        
        allMessages.filter { sms ->
            isBankSms(sms.address, sms.body)
        }.take(limit)
    }
    
    /**
     * Check if an SMS is likely a bank/transaction message.
     */
    fun isBankSms(address: String, body: String): Boolean {
        // Check sender pattern
        val isBankSender = bankSenderPatterns.any { it.matches(address) }
        
        // Check for transaction keywords in body
        val hasTransactionKeywords = transactionKeywords.any { keyword ->
            body.contains(keyword, ignoreCase = true)
        }
        
        // Check for amount pattern
        val hasAmount = amountPattern.containsMatchIn(body)
        
        return (isBankSender && hasTransactionKeywords && hasAmount)
    }
    
    companion object {
        /** Keywords that indicate a transaction SMS */
        val transactionKeywords = listOf(
            "debited",
            "credited",
            "withdrawn",
            "deposited",
            "spent",
            "received",
            "paid",
            "transfer",
            "UPI",
            "IMPS",
            "NEFT",
            "RTGS",
            "A/c",
            "Acct",
            "Account",
            "ATM",
            "payment"
        )
        
        /** Pattern to match amount in various formats */
        val amountPattern = Regex(
            """(?:Rs\.?|INR|₹)\s*[\d,]+(?:\.\d{2})?""",
            RegexOption.IGNORE_CASE
        )
    }
}
