package com.hello.lets.test.screen.ui.analytics

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.Category
import com.hello.lets.test.data.repository.TransactionRepository
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/**
 * Analytics screen showing spending breakdown and trends.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        fontSize = 28.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Month Selector
            MonthSelector(
                currentMonth = uiState.currentMonthName,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total Spent",
                    amount = uiState.totalSpent,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Income",
                    amount = uiState.totalIncome,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Spending by Category
            Text(
                text = "Spending by Category",
                fontSize = 18.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Donut Chart
            if (uiState.categorySpending.isNotEmpty()) {
                SpendingDonutChart(
                    spending = uiState.categorySpending,
                    categories = categories,
                    totalSpent = uiState.totalSpent
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No spending data yet",
                        fontSize = 14.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Breakdown List
            if (uiState.categorySpending.isNotEmpty()) {
                Text(
                    text = "Category Breakdown",
                    fontSize = 18.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                uiState.categorySpending.forEach { (categoryId, amount) ->
                    val category = categories.find { it.id == categoryId }
                    CategoryBreakdownItem(
                        categoryName = category?.name ?: "Uncategorized",
                        amount = amount,
                        percentage = if (uiState.totalSpent > 0) (amount / uiState.totalSpent * 100).toFloat() else 0f,
                        color = getCategoryColor(category?.colorHex)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MonthSelector(
    currentMonth: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Text(
                text = "←",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = currentMonth,
            fontSize = 18.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(onClick = onNext) {
            Text(
                text = "→",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${String.format("%,.0f", amount)}",
                fontSize = 20.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SpendingDonutChart(
    spending: Map<Long?, Double>,
    categories: List<Category>,
    totalSpent: Double
) {
    val colors = spending.map { (categoryId, _) ->
        val category = categories.find { it.id == categoryId }
        getCategoryColor(category?.colorHex)
    }
    
    val sweepAngles = spending.map { (_, amount) ->
        if (totalSpent > 0) (amount / totalSpent * 360f).toFloat() else 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            var startAngle = -90f
            
            sweepAngles.forEachIndexed { index, sweep ->
                drawArc(
                    color = colors.getOrElse(index) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "₹${String.format("%,.0f", totalSpent)}",
                fontSize = 20.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total Spent",
                fontSize = 12.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryBreakdownItem(
    categoryName: String,
    amount: Double,
    percentage: Float,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryName,
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${String.format("%,.0f", amount)}",
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 12.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getCategoryColor(colorHex: String?): Color {
    return try {
        colorHex?.let { Color(android.graphics.Color.parseColor(it)) } 
            ?: Color(0xFF4CAF50)
    } catch (e: Exception) {
        Color(0xFF4CAF50)
    }
}

/**
 * ViewModel for Analytics screen.
 */
class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(
        database.transactionDao(),
        database.categoryDao(),
        database.parsingRuleDao(),
        database.excludedSenderDao()
    )
    
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()
    
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val uiState: StateFlow<AnalyticsUiState> = combine(
        _selectedMonth,
        repository.getTotalSpent(getMonthStart(), getMonthEnd()),
        repository.getTotalIncome(getMonthStart(), getMonthEnd())
    ) { month, spent, income ->
        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        AnalyticsUiState(
            currentMonthName = monthFormat.format(month.time),
            totalSpent = spent,
            totalIncome = income,
            categorySpending = emptyMap() // TODO: Implement category spending query
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AnalyticsUiState()
    )
    
    private fun getMonthStart(): Long {
        val cal = _selectedMonth.value.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    
    private fun getMonthEnd(): Long {
        val cal = _selectedMonth.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return cal.timeInMillis
    }
    
    fun previousMonth() {
        _selectedMonth.value = (_selectedMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
    }
    
    fun nextMonth() {
        _selectedMonth.value = (_selectedMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
    }
}

data class AnalyticsUiState(
    val currentMonthName: String = "",
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val categorySpending: Map<Long?, Double> = emptyMap()
)
