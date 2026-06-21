package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TransactionEntity
import com.example.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

// Geometric Balance Theme Palette Constants
private val GeoBackground = Color(0xFFFDFBFF)
private val GeoTextPrimary = Color(0xFF1B1B1F)
private val GeoTextSecondary = Color(0xFF44474E)
private val GeoTextTertiary = Color(0xFF74777F)
private val GeoCardBackground = Color(0xFFFFFFFF)
private val GeoCardBorder = Color(0xFFC3C6CF)
private val GeoHeaderCardBg = Color(0xFFDDE2F9)
private val GeoIncomeGreen = Color(0xFF006E1C)
private val GeoExpenseRed = Color(0xFFBA1A1A)
private val GeoButtonBg = Color(0xFFD1E4FF)
private val GeoButtonText = Color(0xFF0061A4)
private val GeoScanBadgeBg = Color(0xFFBAF3FF)
private val GeoScanBadgeText = Color(0xFF001F25)

data class CategoryMeta(val icon: String, val color: Color, val containerColor: Color)

val CATEGORY_META = mapOf(
    "Food & Dining" to CategoryMeta("🍽️", Color(0xFFF97316), Color(0xFFFFDBCB)),
    "Groceries" to CategoryMeta("🛒", Color(0xFF22C55E), Color(0xFFD5F3DD)),
    "Transport" to CategoryMeta("🚗", Color(0xFF3B82F6), Color(0xFFD1E4FF)),
    "Entertainment" to CategoryMeta("🎬", Color(0xFFA855F7), Color(0xFFEADBFF)),
    "Health" to CategoryMeta("💊", Color(0xFFEF4444), Color(0xFFFCDDDF)),
    "Shopping" to CategoryMeta("🛍️", Color(0xFFEC4899), Color(0xFFFBD6E8)),
    "ATM / Cash" to CategoryMeta("💵", Color(0xFFF59E0B), Color(0xFFFFF1C5)),
    "Income" to CategoryMeta("💰", Color(0xFF00D4AA), Color(0xFFD9E2FF)),
    "Utilities" to CategoryMeta("⚡", Color(0xFF6366F1), Color(0xFFE0E0FF)),
    "Other" to CategoryMeta("📌", Color(0xFF6B7280), Color(0xFFEAEAEA))
)

val BANK_COLORS = mapOf(
    "CIB" to Color(0xFF001945),
    "NBE" to Color(0xFF00492E),
    "ALEXBANK" to Color(0xFF4C0F80),
    "HSBC" to Color(0xFF6B0E0E),
    "MANUAL" to Color(0xFF323B4B)
)

private fun formatEgp(amount: Double): String {
    return try {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "EG"))
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        formatter.format(amount)
    } catch (e: Exception) {
        amount.toInt().toString()
    }
}

@Composable
fun ExpenseTrackerScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val scanning by viewModel.scanning.collectAsStateWithLifecycle()
    val scannedCount by viewModel.scannedCount.collectAsStateWithLifecycle()
    val selectedTx by viewModel.selectedTx.collectAsStateWithLifecycle()
    val filterCat by viewModel.filterCat.collectAsStateWithLifecycle()
    val addingManual by viewModel.addingManual.collectAsStateWithLifecycle()
    val newSmsText by viewModel.newSmsText.collectAsStateWithLifecycle()
    val parsePreview by viewModel.parsePreview.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavLayout(
                activeTab = tab,
                onTabSelected = { viewModel.setTab(it) }
            )
        },
        containerColor = GeoBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GeoBackground)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                when (tab) {
                    "dashboard" -> {
                        DashboardTab(
                            transactions = transactions,
                            scanning = scanning,
                            scannedCount = scannedCount,
                            onScanClick = { viewModel.runSimulatedScan() },
                            onTxClick = { viewModel.setSelectedTransaction(it) },
                            onSeeAllClick = { viewModel.setTab("transactions") }
                        )
                    }
                    "insights" -> {
                        InsightsTab(
                            transactions = transactions
                        )
                    }
                    "transactions" -> {
                        TransactionsTab(
                            transactions = transactions,
                            selectedFilterCategory = filterCat,
                            onCategoryFilterSelected = { viewModel.setFilterCategory(it) },
                            onAddManualClick = { viewModel.setAddingManual(true) },
                            onTxClick = { viewModel.setSelectedTransaction(it) }
                        )
                    }
                    "settings" -> {
                        SettingsTab(
                            onResetClick = { viewModel.clearAll() }
                        )
                    }
                }
            }

            // --- Dialogs/Sheets ---
            if (addingManual) {
                AddManualDialog(
                    newSmsText = newSmsText,
                    onSmsTextChange = { viewModel.updateSmsText(it) },
                    parsePreview = parsePreview,
                    onParseClick = { viewModel.generateParsePreview() },
                    onConfirmClick = { viewModel.confirmManualInsert() },
                    onDismiss = { viewModel.setAddingManual(false) }
                )
            }

            if (selectedTx != null) {
                TransactionDetailDialog(
                    tx = selectedTx!!,
                    onDeleteClick = {
                        viewModel.deleteTransaction(selectedTx!!)
                        viewModel.setSelectedTransaction(null)
                    },
                    onDismiss = { viewModel.setSelectedTransaction(null) }
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// DASHBOARD VIEW
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun ColumnScope.DashboardTab(
    transactions: List<TransactionEntity>,
    scanning: Boolean,
    scannedCount: Int,
    onScanClick: () -> Unit,
    onTxClick: (TransactionEntity) -> Unit,
    onSeeAllClick: () -> Unit
) {
    val totalIncome = remember(transactions) {
        transactions.filter { it.isCredit }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { !it.isCredit }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    val catBreakdown = remember(transactions) {
        val mapping = mutableMapOf<String, Double>()
        transactions.filter { !it.isCredit }.forEach {
            mapping[it.category] = (mapping[it.category] ?: 0.0) + it.amount
        }
        mapping.entries.sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .testTag("dashboard_scroll_area"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        color = GeoTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Emad Portfolio",
                        color = GeoTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD9E2FF))
                        .border(1.dp, Color(0xFFADC6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EE",
                        color = Color(0xFF001945),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (scanning) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(GeoCardBackground, RoundedCornerShape(12.dp))
                        .border(1.dp, GeoCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GeoButtonText)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reading SMS messages… $scannedCount/8 completed",
                            color = GeoButtonText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val animatedProgress by animateFloatAsState(
                        targetValue = scannedCount / 8f,
                        animationSpec = spring(),
                        label = "ScanProgress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = GeoButtonText,
                        trackColor = GeoHeaderCardBg
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoHeaderCardBg),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL BALANCE",
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(GeoScanBadgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SYNCED",
                                color = GeoScanBadgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "EGP " + formatEgp(netBalance),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GeoTextPrimary,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "INCOME",
                                color = GeoTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "EGP " + formatEgp(totalIncome),
                                color = GeoIncomeGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "EXPENSES",
                                color = GeoTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "EGP " + formatEgp(totalExpense),
                                color = GeoExpenseRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (catBreakdown.isNotEmpty()) {
            item {
                Text(
                    text = "Spending by Category",
                    color = GeoTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(catBreakdown) { entry ->
                val meta = CATEGORY_META[entry.key] ?: CATEGORY_META["Other"]!!
                val pct = if (totalExpense > 0.0) (entry.value / totalExpense).toFloat() else 0.0f
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = meta.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = entry.key,
                            color = GeoTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = meta.color,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }

                    Text(
                        text = "EGP " + formatEgp(entry.value),
                        color = GeoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SMS INSIGHT",
                    color = GeoTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = onScanClick,
                    enabled = !scanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoButtonBg,
                        contentColor = GeoButtonText,
                        disabledContainerColor = GeoTextTertiary.copy(alpha = 0.15f),
                        disabledContentColor = GeoTextTertiary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .testTag("scan_sms_button")
                        .height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync icon",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (scanning) "Scanning…" else "Manual Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (transactions.isEmpty() && !scanning) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap 'Manual Sync' to import Egyptian bank messages",
                        color = GeoTextTertiary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val recents = transactions.take(4)
            items(recents) { tx ->
                TransactionCard(tx = tx, onClick = { onTxClick(tx) })
            }
            if (transactions.size > 4) {
                item {
                    TextButton(
                        onClick = onSeeAllClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "See all ${transactions.size} transactions →",
                            color = GeoButtonText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// TRANSACTIONS LIST VIEW
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun ColumnScope.TransactionsTab(
    transactions: List<TransactionEntity>,
    selectedFilterCategory: String,
    onCategoryFilterSelected: (String) -> Unit,
    onAddManualClick: () -> Unit,
    onTxClick: (TransactionEntity) -> Unit
) {
    val uniqueCategories = remember(transactions) {
        val categories = mutableSetOf("All")
        transactions.forEach { categories.add(it.category) }
        categories.toList().sorted()
    }

    val filteredList = remember(transactions, selectedFilterCategory) {
        if (selectedFilterCategory == "All") {
            transactions
        } else {
            transactions.filter { it.category == selectedFilterCategory }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Transactions",
            color = GeoTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        
        Button(
            onClick = onAddManualClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = GeoButtonBg,
                contentColor = GeoButtonText
            ),
            border = BorderStroke(1.dp, GeoButtonText.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.testTag("add_transaction_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Icon",
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Add",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(uniqueCategories) { category ->
            val active = category == selectedFilterCategory
            val title = if (category == "All") "All" else "${CATEGORY_META[category]?.icon ?: ""} $category"
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (active) GeoButtonBg else Color(0xFFF2F0F4)
                    )
                    .border(
                        1.dp,
                        if (active) GeoButtonText else GeoCardBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategoryFilterSelected(category) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("filter_pill_$category")
            ) {
                Text(
                    text = title,
                    color = if (active) GeoButtonText else GeoTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .testTag("filtered_transactions_scroll"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
    ) {
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching transactions. Scan your messages or copy and paste one manually.",
                        color = Color(0xFF2E4B6A),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredList) { tx ->
                TransactionCard(tx = tx, onClick = { onTxClick(tx) })
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// INSIGHTS VIEW
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun ColumnScope.InsightsTab(
    transactions: List<TransactionEntity>
) {
    val totalExpense = remember(transactions) {
        transactions.filter { !it.isCredit }.sumOf { it.amount }
    }
    val totalIncome = remember(transactions) {
        transactions.filter { it.isCredit }.sumOf { it.amount }
    }
    
    val catBreakdown = remember(transactions) {
        val mapping = mutableMapOf<String, Double>()
        transactions.filter { !it.isCredit }.forEach {
            mapping[it.category] = (mapping[it.category] ?: 0.0) + it.amount
        }
        mapping.entries.sortedByDescending { it.value }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Analytics Insights",
            color = GeoTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }

    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("insights_scroll"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Scan transactions to unlock geometric insights.",
                        color = GeoTextTertiary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GeoHeaderCardBg),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "SAVINGS RATE",
                            color = GeoTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val savingsRate = if (totalIncome > 0) {
                            ((totalIncome - totalExpense) / totalIncome * 100).coerceAtLeast(0.0)
                        } else {
                            0.0
                        }
                        Text(
                            text = "${savingsRate.toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = GeoTextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Based on monthly EGP deposit vs credit spend index.",
                            color = GeoTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
                    border = BorderStroke(1.dp, GeoCardBorder),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "CATEGORY INSIGHTS",
                            color = GeoTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        catBreakdown.forEach { entry ->
                            val meta = CATEGORY_META[entry.key] ?: CATEGORY_META["Other"]!!
                            val pct = if (totalExpense > 0) entry.value / totalExpense else 0.0
                            
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${meta.icon} ${entry.key}",
                                        color = GeoTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "EGP " + formatEgp(entry.value) + " (${(pct * 100).toInt()}%)",
                                        color = GeoTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { pct.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = meta.color,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// SETTINGS VIEW
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun ColumnScope.SettingsTab(
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Settings",
            color = GeoTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }

    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("settings_scroll"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
                border = BorderStroke(1.dp, GeoCardBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "APP CONFIGURATIONS",
                        color = GeoTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Option 1",
                                color = GeoTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Disabled",
                                color = GeoTextTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "nothing under settings",
                            color = GeoTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    HorizontalDivider(color = GeoCardBorder, modifier = Modifier.padding(vertical = 12.dp))

                    // Option 2 (Currency Preference)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Primary Currency",
                            color = GeoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "EGP (Egyptian Pound)",
                            color = GeoButtonText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = GeoCardBorder, modifier = Modifier.padding(vertical = 12.dp))

                    // Option 3 (Visual Theme)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected Theme",
                            color = GeoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Geometric Balance Light",
                            color = GeoButtonText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
                border = BorderStroke(1.dp, GeoCardBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DATA MANAGEMENT",
                        color = GeoTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onResetClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoExpenseRed.copy(alpha = 0.1f),
                            contentColor = GeoExpenseRed
                        ),
                        border = BorderStroke(1.dp, GeoExpenseRed.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_db_button")
                    ) {
                        Text(
                            text = "Reset Database",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// REUSABLE TRANSACTION ROW CARD
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun TransactionCard(
    tx: TransactionEntity,
    onClick: () -> Unit
) {
    val meta = CATEGORY_META[tx.category] ?: CATEGORY_META["Other"]!!
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GeoCardBackground)
            .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("tx_card_${tx.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(meta.containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = meta.icon, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tx.merchant,
                    color = GeoTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                Text(
                    text = (if (tx.isCredit) "+" else "-") + "EGP " + formatEgp(tx.amount),
                    color = if (tx.isCredit) GeoIncomeGreen else GeoExpenseRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F0F4))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tx.bank,
                        color = GeoTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = tx.dateString,
                    color = GeoTextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// MANUAL IMPORT MODAL / SHEET
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun AddManualDialog(
    newSmsText: String,
    onSmsTextChange: (String) -> Unit,
    parsePreview: com.example.data.ParsedTransaction?,
    onParseClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
            border = BorderStroke(1.dp, GeoCardBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Parse Bank SMS",
                    color = GeoTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = newSmsText,
                    onValueChange = onSmsTextChange,
                    placeholder = {
                        Text(
                            text = "Paste your bank SMS alert here e.g.\nCIB: Your account 1234 was debited EGP 450.00 at CAIRO KITCHEN...",
                            color = GeoTextTertiary,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("sms_input_field"),
                    textStyle = TextStyle(color = GeoTextPrimary, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF2F0F4),
                        unfocusedContainerColor = Color(0xFFF2F0F4),
                        focusedBorderColor = GeoButtonText,
                        unfocusedBorderColor = GeoCardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (parsePreview != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .background(Color(0xFFF2F0F4), RoundedCornerShape(12.dp))
                            .border(1.dp, GeoCardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        PreviewRow(label = "Merchant", value = parsePreview.merchant)
                        PreviewRow(
                            label = "Amount",
                            value = (if (parsePreview.isCredit) "+" else "-") + "EGP " + formatEgp(parsePreview.amount),
                            valueColor = if (parsePreview.isCredit) GeoIncomeGreen else GeoExpenseRed
                        )
                        PreviewRow(
                            label = "Category",
                            value = "${CATEGORY_META[parsePreview.category]?.icon ?: ""} ${parsePreview.category}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2F0F4),
                            contentColor = GeoTextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_manual_button")
                    ) {
                        Text(text = "Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (parsePreview == null) {
                        Button(
                            onClick = onParseClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeoButtonBg,
                                contentColor = GeoButtonText
                            ),
                            shape = RoundedCornerShape(14.dp),
                            enabled = newSmsText.isNotBlank(),
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp)
                                .testTag("parse_sms_button")
                        ) {
                            Text(text = "Parse SMS", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        Button(
                            onClick = onConfirmClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeoButtonBg,
                                contentColor = GeoButtonText
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp)
                                .testTag("confirm_manual_button")
                        ) {
                            Text(text = "Add Transaction", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewRow(label: String, value: String, valueColor: Color = GeoTextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = GeoTextSecondary, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ──────────────────────────────────────────────────────────────────────────
// DETAILED SLIDE POPUP
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun TransactionDetailDialog(
    tx: TransactionEntity,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val meta = CATEGORY_META[tx.category] ?: CATEGORY_META["Other"]!!
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
            border = BorderStroke(1.dp, GeoCardBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(meta.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = meta.icon, fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = (if (tx.isCredit) "+" else "-") + "EGP " + formatEgp(tx.amount),
                    color = if (tx.isCredit) GeoIncomeGreen else GeoExpenseRed,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = tx.merchant,
                    color = GeoTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = GeoCardBorder)
                    DetailRow(key = "Date", value = tx.dateString)
                    DetailRow(key = "Category", value = "${meta.icon} ${tx.category}")
                    DetailRow(key = "Bank", value = tx.bank)
                    DetailRow(key = "Type", value = if (tx.isCredit) "Credit / Income" else "Debit / Expense")
                    HorizontalDivider(color = GeoCardBorder)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F0F4), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = tx.rawString,
                        color = GeoTextTertiary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoExpenseRed.copy(alpha = 0.1f),
                            contentColor = GeoExpenseRed
                        ),
                        border = BorderStroke(1.dp, GeoExpenseRed.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(text = "Delete", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoButtonBg,
                            contentColor = GeoButtonText
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("close_detail_button")
                    ) {
                        Text(text = "Close", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = key, color = GeoTextSecondary, fontSize = 13.sp)
        Text(text = value, color = GeoTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ──────────────────────────────────────────────────────────────────────────
// BOTTOM NAVIGATION BAR
// ──────────────────────────────────────────────────────────────────────────
@Composable
fun BottomNavLayout(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Column {
        HorizontalDivider(color = GeoCardBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F0F4))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                TabItem("dashboard", "🏠", "Home"),
                TabItem("insights", "📊", "Insights"),
                TabItem("transactions", "📨", "History"),
                TabItem("settings", "⚙️", "Settings")
            ).forEach { item ->
                val active = item.key == activeTab
                
                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(item.key) }
                        .padding(vertical = 2.dp)
                        .testTag("tab_${item.key}_button")
                        .widthIn(min = 72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) GeoButtonBg else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.icon,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        color = if (active) Color(0xFF001D36) else GeoTextSecondary.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

data class TabItem(val key: String, val icon: String, val label: String)
