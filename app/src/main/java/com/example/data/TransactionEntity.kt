package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val isCredit: Boolean,
    val merchant: String,
    val category: String,
    val bank: String,
    val dateString: String,
    val timestamp: Long,
    val rawString: String
)
