package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _tab = MutableStateFlow("dashboard")
    val tab: StateFlow<String> = _tab.asStateFlow()

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    private val _scannedCount = MutableStateFlow(0)
    val scannedCount: StateFlow<Int> = _scannedCount.asStateFlow()

    private val _selectedTx = MutableStateFlow<TransactionEntity?>(null)
    val selectedTx: StateFlow<TransactionEntity?> = _selectedTx.asStateFlow()

    private val _filterCat = MutableStateFlow("All")
    val filterCat: StateFlow<String> = _filterCat.asStateFlow()

    private val _newSmsText = MutableStateFlow("")
    val newSmsText: StateFlow<String> = _newSmsText.asStateFlow()

    private val _addingManual = MutableStateFlow(false)
    val addingManual: StateFlow<Boolean> = _addingManual.asStateFlow()

    private val _parsePreview = MutableStateFlow<ParsedTransaction?>(null)
    val parsePreview: StateFlow<ParsedTransaction?> = _parsePreview.asStateFlow()

    private var scanJob: Job? = null

    init {
        // Auto-scan on launch if database is empty to emulate the web prototype onboarding
        viewModelScope.launch {
            delay(600)
            repository.allTransactions.first().let { currentList ->
                if (currentList.isEmpty()) {
                    runSimulatedScan()
                }
            }
        }
    }

    fun setTab(selectedTab: String) {
        _tab.value = selectedTab
    }

    fun setFilterCategory(category: String) {
        _filterCat.value = category
    }

    fun setSelectedTransaction(tx: TransactionEntity?) {
        _selectedTx.value = tx
    }

    fun setAddingManual(isOpen: Boolean) {
        _addingManual.value = isOpen
        if (!isOpen) {
            _newSmsText.value = ""
            _parsePreview.value = null
        }
    }

    fun updateSmsText(text: String) {
        _newSmsText.value = text
        _parsePreview.value = null // Clear preview until parsed
    }

    fun runSimulatedScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _scanning.value = true
            _scannedCount.value = 0
            
            // Re-simulate clear and scan
            repository.clearAll()
            
            val messagesList = SmsSampleData.messages
            for (i in messagesList.indices) {
                delay(280) // Delay to mimic message receipt and OCR crawling
                val sample = messagesList[i]
                val parsed = SmsParser.parseSMS(
                    rawSms = sample.raw,
                    customTime = sample.timestamp,
                    bankName = sample.bank
                )
                
                val entity = TransactionEntity(
                    amount = parsed.amount,
                    isCredit = parsed.isCredit,
                    merchant = parsed.merchant,
                    category = parsed.category,
                    bank = parsed.bank,
                    dateString = parsed.dateString,
                    timestamp = parsed.timestamp,
                    rawString = parsed.rawString
                )
                repository.insert(entity)
                _scannedCount.value = i + 1
            }
            _scanning.value = false
        }
    }

    fun generateParsePreview() {
        val text = _newSmsText.value
        if (text.isBlank()) return
        val parsed = SmsParser.parseSMS(text, System.currentTimeMillis(), "MANUAL")
        _parsePreview.value = parsed
    }

    fun confirmManualInsert() {
        val parsed = _parsePreview.value ?: return
        viewModelScope.launch {
            val entity = TransactionEntity(
                amount = parsed.amount,
                isCredit = parsed.isCredit,
                merchant = parsed.merchant,
                category = parsed.category,
                bank = parsed.bank,
                dateString = parsed.dateString,
                timestamp = parsed.timestamp,
                rawString = parsed.rawString
            )
            repository.insert(entity)
            setAddingManual(false)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(tx)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
