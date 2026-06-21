package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun insert(transaction: TransactionEntity): Long {
        return dao.insertTransaction(transaction)
    }

    suspend fun insertIfNotExists(transaction: TransactionEntity): Boolean {
        return if (!dao.existsByRawString(transaction.rawString)) {
            dao.insertTransaction(transaction)
            true
        } else {
            false
        }
    }

    suspend fun delete(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    suspend fun clearAll() {
        dao.clearAllTransactions()
    }
}
