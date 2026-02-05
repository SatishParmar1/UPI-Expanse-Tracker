package com.hello.lets.test.screen.ui.search

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Search screen for finding transactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.search(it) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { 
                            Text(
                                "Search transactions...",
                                fontFamily = LiterataFontFamily
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        )
                    )
                }
                
                // Filter chips
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == SearchFilter.ALL,
                            onClick = { viewModel.setFilter(SearchFilter.ALL) },
                            label = { Text("All", fontFamily = LiterataFontFamily) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == SearchFilter.DEBIT,
                            onClick = { viewModel.setFilter(SearchFilter.DEBIT) },
                            label = { Text("Sent", fontFamily = LiterataFontFamily) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.2f)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == SearchFilter.CREDIT,
                            onClick = { viewModel.setFilter(SearchFilter.CREDIT) },
                            label = { Text("Received", fontFamily = LiterataFontFamily) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryGreen.copy(alpha = 0.2f)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == SearchFilter.TODAY,
                            onClick = { viewModel.setFilter(SearchFilter.TODAY) },
                            label = { Text("Today", fontFamily = LiterataFontFamily) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == SearchFilter.THIS_WEEK,
                            onClick = { viewModel.setFilter(SearchFilter.THIS_WEEK) },
                            label = { Text("This Week", fontFamily = LiterataFontFamily) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recent searches
            if (uiState.searchQuery.isEmpty() && uiState.recentSearches.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Searches",
                        fontSize = 14.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.recentSearches) { search ->
                    RecentSearchItem(
                        query = search,
                        onClick = { viewModel.search(search) }
                    )
                }
            }
            
            // Search results
            if (uiState.searchQuery.isNotEmpty()) {
                item {
                    Text(
                        text = "${uiState.results.size} results",
                        fontSize = 14.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            items(uiState.results) { transaction ->
                SearchResultItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) }
                )
            }
            
            // Empty state
            if (uiState.searchQuery.isNotEmpty() && uiState.results.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions found",
                                fontSize = 16.sp,
                                fontFamily = LiterataFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun RecentSearchItem(
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = query,
            fontSize = 14.sp,
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SearchResultItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val primaryGreen = Color(0xFF4CAF50)
    val dangerRed = Color(0xFFEF4444)
    val isDebit = transaction.transactionType == TransactionType.DEBIT
    val amountColor = if (isDebit) dangerRed else primaryGreen
    val amountPrefix = if (isDebit) "-" else "+"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(amountColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDebit) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column {
                    Text(
                        text = transaction.merchant,
                        fontSize = 15.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    
                    Text(
                        text = formatDate(transaction.transactionDate),
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "$amountPrefix₹${String.format("%,.0f", transaction.amount)}",
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// Data classes
enum class SearchFilter { ALL, DEBIT, CREDIT, TODAY, THIS_WEEK }

data class SearchUiState(
    val searchQuery: String = "",
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val results: List<Transaction> = emptyList(),
    val recentSearches: List<String> = listOf("Amazon", "Swiggy", "PhonePe"),
    val isLoading: Boolean = false
)

// ViewModel
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var allTransactions: List<Transaction> = emptyList()
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactions().collect { transactions ->
                allTransactions = transactions
                applyFilters()
            }
        }
    }
    
    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", results = emptyList())
    }
    
    fun setFilter(filter: SearchFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilters()
    }
    
    private fun applyFilters() {
        val query = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.selectedFilter
        
        var filtered = allTransactions
        
        // Apply text search
        if (query.isNotEmpty()) {
            filtered = filtered.filter { transaction ->
                transaction.merchant.lowercase().contains(query) ||
                transaction.notes?.lowercase()?.contains(query) == true ||
                transaction.merchant.lowercase().contains(query) ||
                transaction.amount.toString().contains(query)
            }
        }
        
        // Apply type filter
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        filtered = when (filter) {
            SearchFilter.DEBIT -> filtered.filter { it.transactionType == TransactionType.DEBIT }
            SearchFilter.CREDIT -> filtered.filter { it.transactionType == TransactionType.CREDIT }
            SearchFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val todayStart = calendar.timeInMillis
                filtered.filter { it.transactionDate >= todayStart }
            }
            SearchFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val weekStart = calendar.timeInMillis
                filtered.filter { it.transactionDate >= weekStart }
            }
            SearchFilter.ALL -> filtered
        }
        
        _uiState.value = _uiState.value.copy(results = filtered.take(50))
    }
}
