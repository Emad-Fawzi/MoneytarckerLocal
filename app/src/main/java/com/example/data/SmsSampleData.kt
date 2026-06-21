package com.example.data

import java.text.SimpleDateFormat
import java.util.Locale

data class SampleSms(
    val id: Int,
    val raw: String,
    val bank: String,
    val isoTimestamp: String
) {
    val timestamp: Long
        get() {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                sdf.parse(isoTimestamp)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
}

object SmsSampleData {
    val messages = listOf(
        SampleSms(
            id = 1,
            raw = "CIB: Your account 1234 was debited EGP 450.00 at CAIRO KITCHEN on 20/06/2026 at 14:32. Available balance: EGP 12,300.50",
            bank = "CIB",
            isoTimestamp = "2026-06-20T14:32:00"
        ),
        SampleSms(
            id = 2,
            raw = "NBE: Debit card purchase EGP 1,200.00 at CARREFOUR MAADI 20/06/2026 10:15. Bal: EGP 8,750.00",
            bank = "NBE",
            isoTimestamp = "2026-06-20T10:15:00"
        ),
        SampleSms(
            id = 3,
            raw = "CIB: Your account 1234 credited EGP 25,000.00 - Salary Transfer on 19/06/2026. Available balance: EGP 37,550.50",
            bank = "CIB",
            isoTimestamp = "2026-06-19T09:00:00"
        ),
        SampleSms(
            id = 4,
            raw = "ALEXBANK: POS Purchase EGP 320.50 UBER EGYPT 18/06/2026 22:44. Remaining balance 5,430.20 EGP",
            bank = "ALEXBANK",
            isoTimestamp = "2026-06-18T22:44:00"
        ),
        SampleSms(
            id = 5,
            raw = "NBE: ATM Withdrawal EGP 2,000.00 from ATM NASR CITY 18/06/2026 18:05. Balance: EGP 6,750.00",
            bank = "NBE",
            isoTimestamp = "2026-06-18T18:05:00"
        ),
        SampleSms(
            id = 6,
            raw = "CIB: Your account 1234 was debited EGP 89.00 at NETFLIX on 17/06/2026 at 00:01. Available balance: EGP 12,461.50",
            bank = "CIB",
            isoTimestamp = "2026-06-17T00:01:00"
        ),
        SampleSms(
            id = 7,
            raw = "HSBC: Payment of EGP 5,400.00 received for Credit Card ending 5678 on 17/06/2026. Thank you.",
            bank = "HSBC",
            isoTimestamp = "2026-06-17T11:30:00"
        ),
        SampleSms(
            id = 8,
            raw = "CIB: Your account 1234 was debited EGP 215.00 at PHARMACIE SEIF on 16/06/2026 at 09:22. Available balance: EGP 17,861.50",
            bank = "CIB",
            isoTimestamp = "2026-06-16T09:22:00"
        )
    )
}
