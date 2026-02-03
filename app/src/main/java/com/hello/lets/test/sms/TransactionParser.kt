package com.hello.lets.test.sms

import com.hello.lets.test.data.entity.TransactionType

/**
 * Parses SMS content to extract transaction information.
 * Handles various Indian bank SMS formats.
 */
class TransactionParser {
    
    /**
     * Result of parsing an SMS message.
     */
    data class ParsedTransaction(
        val amount: Double?,
        val merchant: String?,
        val transactionType: TransactionType?,
        val referenceId: String?,
        val accountNumber: String?,
        val balance: Double?
    )
    
    // Keywords indicating money was debited (spent)
    private val debitKeywords = listOf(
        "debited", "debit", "spent", "paid", "withdrawn", 
        "purchase", "transferred", "sent", "dr"
    )
    
    // Keywords indicating money was credited (received)
    private val creditKeywords = listOf(
        "credited", "credit", "received", "deposited", 
        "refund", "cashback", "cr"
    )
    
    // Amount extraction patterns
    private val amountPatterns = listOf(
        // Rs. 350.00 or Rs 350 or Rs.350.00
        Regex("""Rs\.?\s*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // INR 350.00
        Regex("""INR\s*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // ₹350.00 or ₹ 350
        Regex("""₹\s*([\d,]+(?:\.\d{2})?)"""),
        // "debited for 350.00" or "credited with 350"
        Regex("""(?:debited|credited|spent|paid|received|withdrawn)\s+(?:for\s+|with\s+|of\s+)?(?:Rs\.?|INR|₹)?\s*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        // Amount of Rs 350
        Regex("""amount\s+(?:of\s+)?(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
    )
    
    // UPI/IMPS/NEFT reference patterns
    private val referencePatterns = listOf(
        Regex("""(?:UPI|IMPS|NEFT|RTGS)\s*(?:Ref|ID|No|:)?[\s:]*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE),
        Regex("""Ref\s*(?:No|ID)?[\s:]*([A-Za-z0-9]{6,})""", RegexOption.IGNORE_CASE),
        Regex("""Transaction\s*(?:ID|No)?[\s:]*([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE)
    )
    
    // Account number patterns (usually last 4 digits)
    private val accountPatterns = listOf(
        Regex("""(?:A/c|Acct|Account|AC)\s*[:\s]*(?:\*+|X+|x+)?(\d{4})""", RegexOption.IGNORE_CASE),
        Regex("""(?:ending|linked)\s+(?:with\s+)?(\d{4})""", RegexOption.IGNORE_CASE),
        Regex("""\*+(\d{4})"""),
        Regex("""XX(\d{4})""", RegexOption.IGNORE_CASE)
    )
    
    // Balance patterns
    private val balancePatterns = listOf(
        Regex("""(?:Bal|Balance|Avl\.?\s*Bal|Available\s*Balance)[:\s]*(?:Rs\.?|INR|₹)?\s*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{2})?)\s*(?:available|avl)""", RegexOption.IGNORE_CASE)
    )
    
    // Merchant extraction patterns
    private val merchantPatterns = listOf(
        // "at Swiggy" or "to Amazon" or "from Uber"
        Regex("""(?:at|to|from|for|@)\s+([A-Za-z0-9][A-Za-z0-9\s&'.,-]{1,30}?)(?:\s+(?:on|via|Ref|UPI|using|through|\.|$))""", RegexOption.IGNORE_CASE),
        // VPA patterns: swiggy@paytm or merchant.upi@bank
        Regex("""VPA\s*[:\s]*([a-zA-Z0-9._-]+)@""", RegexOption.IGNORE_CASE),
        // "Info: Swiggy" or "Info-Swiggy"
        Regex("""Info[:\s-]+([A-Za-z0-9][A-Za-z0-9\s&'.-]{1,25})""", RegexOption.IGNORE_CASE),
        // Merchant after amount: "Rs.350 Swiggy"
        Regex("""(?:Rs\.?|INR|₹)\s*[\d,]+(?:\.\d{2})?\s+([A-Za-z][A-Za-z0-9\s]{2,20})""", RegexOption.IGNORE_CASE)
    )
    
    /**
     * Parse an SMS body to extract transaction details.
     */
    fun parse(smsBody: String): ParsedTransaction {
        return ParsedTransaction(
            amount = extractAmount(smsBody),
            merchant = extractMerchant(smsBody)?.trim()?.take(50),
            transactionType = detectTransactionType(smsBody),
            referenceId = extractReferenceId(smsBody),
            accountNumber = extractAccountNumber(smsBody),
            balance = extractBalance(smsBody)
        )
    }
    
    /**
     * Extract the transaction amount from SMS text.
     */
    private fun extractAmount(text: String): Double? {
        for (pattern in amountPatterns) {
            pattern.find(text)?.let { match ->
                val amountStr = match.groupValues.getOrNull(1)
                    ?.replace(",", "")
                    ?.trim()
                amountStr?.toDoubleOrNull()?.let { return it }
            }
        }
        return null
    }
    
    /**
     * Detect if the transaction is a debit or credit.
     */
    private fun detectTransactionType(text: String): TransactionType? {
        val lowerText = text.lowercase()
        
        // Check debit keywords first (more common)
        if (debitKeywords.any { lowerText.contains(it) }) {
            return TransactionType.DEBIT
        }
        
        // Check credit keywords
        if (creditKeywords.any { lowerText.contains(it) }) {
            return TransactionType.CREDIT
        }
        
        return null
    }
    
    /**
     * Extract UPI/IMPS/NEFT reference ID.
     */
    private fun extractReferenceId(text: String): String? {
        for (pattern in referencePatterns) {
            pattern.find(text)?.let { match ->
                val refId = match.groupValues.getOrNull(1)?.trim()
                if (refId != null && refId.length >= 6) {
                    return refId
                }
            }
        }
        return null
    }
    
    /**
     * Extract account number (usually last 4 digits).
     */
    private fun extractAccountNumber(text: String): String? {
        for (pattern in accountPatterns) {
            pattern.find(text)?.let { match ->
                return match.groupValues.getOrNull(1)?.trim()
            }
        }
        return null
    }
    
    /**
     * Extract balance after transaction.
     */
    private fun extractBalance(text: String): Double? {
        for (pattern in balancePatterns) {
            pattern.find(text)?.let { match ->
                val balanceStr = match.groupValues.getOrNull(1)
                    ?.replace(",", "")
                    ?.trim()
                balanceStr?.toDoubleOrNull()?.let { return it }
            }
        }
        return null
    }
    
    /**
     * Extract merchant/vendor name.
     */
    private fun extractMerchant(text: String): String? {
        for (pattern in merchantPatterns) {
            pattern.find(text)?.let { match ->
                val merchant = match.groupValues.getOrNull(1)?.trim()
                if (merchant != null && merchant.length >= 2 && !isCommonWord(merchant)) {
                    return cleanMerchantName(merchant)
                }
            }
        }
        return null
    }
    
    /**
     * Clean up extracted merchant name.
     */
    private fun cleanMerchantName(name: String): String {
        return name
            .replace(Regex("""\s+"""), " ")  // Normalize whitespace
            .replace(Regex("""[,.]$"""), "") // Remove trailing punctuation
            .trim()
            .split(" ")
            .take(3) // Maximum 3 words
            .joinToString(" ")
    }
    
    /**
     * Check if a word is too common to be a merchant name.
     */
    private fun isCommonWord(word: String): Boolean {
        val commonWords = setOf(
            "the", "and", "for", "your", "with", "from", "has", "been",
            "was", "are", "via", "upi", "imps", "neft", "rtgs", "ref",
            "transaction", "payment", "transfer", "bank", "account"
        )
        return commonWords.contains(word.lowercase())
    }
}
