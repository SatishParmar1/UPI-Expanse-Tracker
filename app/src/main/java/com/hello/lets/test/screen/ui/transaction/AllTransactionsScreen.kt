package com.hello.lets.test.screen.ui.transaction

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.Transaction
import com.hello.lets.test.data.entity.TransactionType
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * All Transactions screen showing full transaction list with filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    viewModel: AllTransactionsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ALL",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Transactions",
                            fontSize = 22.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = uiState.filter == TransactionFilter.ALL,
                    onClick = { viewModel.setFilter(TransactionFilter.ALL) },
                    label = { Text("All", fontFamily = LiterataFontFamily) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryGreen.copy(alpha = 0.2f),
                        selectedLabelColor = primaryGreen
                    )
                )
                FilterChip(
                    selected = uiState.filter == TransactionFilter.DEBITS,
                    onClick = { viewModel.setFilter(TransactionFilter.DEBITS) },
                    label = { Text("Debits", fontFamily = LiterataFontFamily) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFEF4444)
                    )
                )
                FilterChip(
                    selected = uiState.filter == TransactionFilter.CREDITS,
                    onClick = { viewModel.setFilter(TransactionFilter.CREDITS) },
                    label = { Text("Credits", fontFamily = LiterataFontFamily) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryGreen.copy(alpha = 0.2f),
                        selectedLabelColor = primaryGreen
                    )
                )
            }
            
            // Transaction Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${transactions.size} transactions",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No transactions yet",
                            fontSize = 18.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Sync your SMS to see transactions",
                            fontSize = 14.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionListItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) }
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isCredit = transaction.transactionType == TransactionType.CREDIT
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Merchant Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCredit) primaryGreen.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.merchant.take(2).uppercase(),
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) primaryGreen else Color(0xFFEF4444)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatDate(transaction.transactionDate),
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${String.format(Locale.US, "%,.0f", transaction.amount)}",
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) primaryGreen else MaterialTheme.colorScheme.onSurface
                )
                
                // Type Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCredit) primaryGreen.copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isCredit) "CREDIT" else "DEBIT",
                        fontSize = 9.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) primaryGreen else Color(0xFFEF4444),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

enum class TransactionFilter {
    ALL, DEBITS, CREDITS
}

// ViewModel
class AllTransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    
    private val _uiState = MutableStateFlow(AllTransactionsUiState())
    val uiState: StateFlow<AllTransactionsUiState> = _uiState.asStateFlow()
    
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _allTransactions.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactions().collect { allTransactions ->
                val filtered = when (_uiState.value.filter) {
                    TransactionFilter.ALL -> allTransactions
                    TransactionFilter.DEBITS -> allTransactions.filter { it.transactionType == TransactionType.DEBIT }
                    TransactionFilter.CREDITS -> allTransactions.filter { it.transactionType == TransactionType.CREDIT }
                }
                _allTransactions.value = filtered
            }
        }
    }
    
    fun setFilter(filter: TransactionFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        loadTransactions()
    }
}

data class AllTransactionsUiState(
    val filter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = ""
)
