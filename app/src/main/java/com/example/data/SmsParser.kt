package com.example.data

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class ParsedTransaction(
    val amount: Double,
    val isCredit: Boolean,
    val merchant: String,
    val category: String,
    val bank: String,
    val dateString: String,
    val timestamp: Long,
    val rawString: String
)

object SmsParser {
    fun parseSMS(rawSms: String, customTime: Long? = null, bankName: String? = null): ParsedTransaction {
        val text = rawSms.lowercase()
        
        // 1. Extract Amount (e.g. EGP 450.00, 450.00 EGP, 450 EGP, etc.)
        val amountRegex = Regex("""EGP\s?([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        val amountMatch = amountRegex.find(rawSms)
        val amount = if (amountMatch != null) {
            amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: 0.0
        } else {
            // Try matching general numbers prefixing/suffixing with EGP or just decimal
            val egpSuffixRegex = Regex("""([\d,]+\.?\d*)\s?EGP""", RegexOption.IGNORE_CASE)
            val suffixMatch = egpSuffixRegex.find(rawSms)
            if (suffixMatch != null) {
                suffixMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: 0.0
            } else {
                0.0
            }
        }

        // 2. Determine if it is Credit or Debit
        // isCredit = /credit|salary|received|payment.*received/i.test(text) && !/debit|purchase|withdrawal|debited/i.test(text)
        val isCredit = (text.contains("credit") || 
                        text.contains("salary") || 
                        text.contains("received") || 
                        text.contains("credited") ||
                        Regex("""payment.*received""", RegexOption.IGNORE_CASE).containsMatchIn(text) ||
                        text.contains("received for credit card")) &&
                !(text.contains("debit") || 
                  text.contains("purchase") || 
                  text.contains("withdrawal") || 
                  text.contains("debited"))

        // ATM detection
        val isATM = text.contains("atm") || text.contains("withdrawal")

        // 3. Guessed bank name
        val guessedBank = bankName ?: when {
            rawSms.startsWith("CIB", ignoreCase = true) -> "CIB"
            rawSms.startsWith("NBE", ignoreCase = true) -> "NBE"
            rawSms.startsWith("ALEXBANK", ignoreCase = true) -> "ALEXBANK"
            rawSms.startsWith("ALEX", ignoreCase = true) -> "ALEXBANK"
            rawSms.startsWith("HSBC", ignoreCase = true) -> "HSBC"
            else -> {
                // Look for bank tag anywhere in prefix or body
                when {
                    text.contains("cib") -> "CIB"
                    text.contains("nbe") -> "NBE"
                    text.contains("alexbank") -> "ALEXBANK"
                    text.contains("hsbc") -> "HSBC"
                    else -> "MANUAL"
                }
            }
        }

        // 4. Extract Merchant/Description
        var merchant = "Unknown"
        val merchantPatterns = listOf(
            Regex("""at\s+([A-Za-z][A-Za-z\s0-9]+?)(?:\s+on|\s+\d{2}/)""", RegexOption.IGNORE_CASE),
            Regex("""at\s+([A-Za-z][A-Za-z\s0-9]+?)(?:\s*\d{2}/)""", RegexOption.IGNORE_CASE),
            Regex("""([A-Za-z][A-Za-z\s0-9]{3,})(?:\s+\d{2}/\d{2}/)""", RegexOption.IGNORE_CASE),
            Regex("""ATM\s+([A-Za-z][A-Za-z\s0-9]+?)(?:\s+\d{2}/)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in merchantPatterns) {
            val match = pattern.find(rawSms)
            if (match != null) {
                val candidate = match.groupValues[1].trim().replace(Regex("""\s+"""), " ")
                if (candidate.isNotEmpty() && !candidate.equals("egp", ignoreCase = true)) {
                    merchant = candidate
                    break
                }
            }
        }

        if (isCredit && text.contains("salary")) {
            merchant = "Salary Transfer"
        } else if (isCredit && Regex("""payment.*credit card""", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            merchant = "Credit Card Payment"
        } else if (isATM) {
            merchant = if (merchant == "Unknown" || merchant == "EGP") "ATM Withdrawal" else "ATM - $merchant"
        }

        // Clean merchant
        if (merchant == "Unknown" && text.contains("netflix")) {
            merchant = "NETFLIX"
        } else if (merchant == "Unknown" && text.contains("uber")) {
            merchant = "UBER EGYPT"
        } else if (merchant == "Unknown" && text.contains("carrefour")) {
            merchant = "CARREFOUR MAADI"
        }

        // 5. Categorize based on keywords
        val categories = mapOf(
            "Food & Dining" to Regex("""kitchen|restaurant|cafe|food|pizza|kfc|mcdonald|hardee|burger""", RegexOption.IGNORE_CASE),
            "Groceries" to Regex("""carrefour|metro|hyperone|seoudi|spinney""", RegexOption.IGNORE_CASE),
            "Transport" to Regex("""uber|careem|taxi|fuel|gas|petrol|cairo parking""", RegexOption.IGNORE_CASE),
            "Entertainment" to Regex("""netflix|spotify|cinema|vodafone""", RegexOption.IGNORE_CASE),
            "Health" to Regex("""pharma|clinic|hospital|doctor|lab""", RegexOption.IGNORE_CASE),
            "Shopping" to Regex("""mall|fashion|nike|zara|h&m""", RegexOption.IGNORE_CASE),
            "ATM / Cash" to Regex("""atm|withdrawal""", RegexOption.IGNORE_CASE),
            "Income" to Regex("""salary|transfer in|received""", RegexOption.IGNORE_CASE),
            "Utilities" to Regex("""water|electric|gas bill|telecom""", RegexOption.IGNORE_CASE)
        )

        var category = "Other"
        for ((cat, regex) in categories) {
            if (regex.containsMatchIn(rawSms)) {
                category = cat
                break
            }
        }
        if (isCredit && category == "Other") {
            category = "Income"
        }

        // 6. Formatting dates & timestamps
        val dateRegex = Regex("""(\d{2})/(\d{2})/(\d{4})""")
        val dateMatch = dateRegex.find(rawSms)
        var dateStr = ""
        var ts = customTime ?: System.currentTimeMillis()

        if (dateMatch != null) {
            val day = dateMatch.groupValues[1]
            val month = dateMatch.groupValues[2]
            val year = dateMatch.groupValues[3]
            dateStr = "$day/$month/$year"
            
            // Try to set precise hour and minute from the text if present e.g. "at 14:32" or "10:15"
            val hourRegex = Regex("""(?:at\s+)?(\d{2}):(\d{2})""", RegexOption.IGNORE_CASE)
            val timeMatch = hourRegex.find(rawSms)
            var hour = 12
            var minute = 0
            if (timeMatch != null) {
                hour = timeMatch.groupValues[1].toIntOrNull() ?: 12
                minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
            }

            try {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year.toInt())
                cal.set(Calendar.MONTH, month.toInt() - 1)
                cal.set(Calendar.DAY_OF_MONTH, day.toInt())
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                ts = cal.timeInMillis
            } catch (e: Exception) {
                Log.e("SmsParser", "Error parsing date string", e)
            }
        } else {
            // Format today's date if no date string found
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            dateStr = sdf.format(Date(ts))
        }

        return ParsedTransaction(
            amount = amount,
            isCredit = isCredit,
            merchant = merchant,
            category = category,
            bank = guessedBank.uppercase(),
            dateString = dateStr,
            timestamp = ts,
            rawString = rawSms
        )
    }
}
