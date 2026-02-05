package com.hello.lets.test.screen.ui.analytics

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.entity.Budget
import com.hello.lets.test.data.entity.BudgetType
import com.hello.lets.test.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.hello.lets.test.ui.theme.LiterataFontFamily

/**
 * Analytics screen with spending insights and budget performance.
 */
@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel(),
    onViewReport: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    
    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { amount, type ->
                viewModel.addBudget(amount, type)
                showAddBudgetDialog = false
            }
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ANALYTICS",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Budget Performance",
                            fontSize = 24.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddBudgetDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Budget",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Budget Type Filter Tabs
            item {
                BudgetTypeFilterTabs(
                    selectedFilter = uiState.selectedBudgetFilter,
                    onFilterChange = { viewModel.setBudgetFilter(it) },
                    dailyCount = uiState.dailyBudgetCount,
                    weeklyCount = uiState.weeklyBudgetCount,
                    monthlyCount = uiState.monthlyBudgetCount
                )
            }
            
            // Budget List Section with Edit/Delete
            item {
                BudgetListSection(
                    budgets = uiState.activeBudgets,
                    onEdit = { budget -> viewModel.editBudget(budget) },
                    onDelete = { viewModel.deleteBudget(it) }
                )
            }
            
            // Budget Circle Card (Summary of Monthly Budget if available, else static)
            item {
                BudgetCircleCard(
                    spentAmount = uiState.totalSpent,
                    budgetAmount = uiState.budgetAmount,
                    remainingAmount = uiState.remainingBudget,
                    dailyAverage = uiState.dailyAverage
                )
            }
            
            // Spending Trends Section
            item {
                SpendingTrendsSection(
                    weeklyData = uiState.weeklySpending,
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodChange = { viewModel.setSelectedPeriod(it) },
                    dateRange = uiState.dateRange
                )
            }
            
            // Top Merchants Section
            item {
                TopMerchantsSection(
                    merchants = uiState.topMerchants,
                    onViewReport = onViewReport
                )
            }
            
            // Category Breakdown
            item {
                CategoryBreakdownSection(
                    categories = uiState.categoryBreakdown
                )
            }
            
            // Bottom spacing
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BudgetCircleCard(
    spentAmount: Double,
    budgetAmount: Double,
    remainingAmount: Double,
    dailyAverage: Double
) {
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val progress = if (budgetAmount > 0) (spentAmount / budgetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(surfaceColor)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Background arc
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Progress arc
                    val progressColor = if (animatedProgress > 0.8f) Color(0xFFEF4444) else primaryGreen
                    drawArc(
                        color = progressColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Spent This Month",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${formatAmount(spentAmount)}",
                        fontSize = 32.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(primaryGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}% of Budget",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = primaryGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "REMAINING",
                    value = "₹${formatAmount(remainingAmount)}"
                )
                StatItem(
                    label = "DAILY AVG",
                    value = "₹${formatAmount(dailyAverage)}"
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SpendingTrendsSection(
    weeklyData: List<DailySpending>,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    dateRange: String
) {
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spending Trends",
                fontSize = 20.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Period Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .padding(4.dp)
            ) {
                listOf("Week", "Month").forEach { period ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selectedPeriod == period) primaryGreen else Color.Transparent)
                            .clickable { onPeriodChange(period) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = period,
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedPeriod == period) Color.White 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceColor)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateRange,
                        fontSize = 13.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        LegendItem("Safe", primaryGreen)
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem("High", Color(0xFFEF4444))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bar Chart
                WeeklyBarChart(data = weeklyData)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklyBarChart(data: List<DailySpending>) {
    val primaryGreen = Color(0xFF4CAF50)
    val maxAmount = data.maxOfOrNull { it.amount } ?: 1.0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            val barHeight = ((day.amount / maxAmount) * 100).dp.coerceAtLeast(8.dp)
            val isHigh = day.amount > maxAmount * 0.7
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tooltip for selected day
                if (day.isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "₹${formatAmount(day.amount)}",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (isHigh) Color(0xFFEF4444) else primaryGreen)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = day.dayLabel,
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (day.isSelected) MaterialTheme.colorScheme.onSurface 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TopMerchantsSection(
    merchants: List<MerchantSpending>,
    onViewReport: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Merchants",
                fontSize = 20.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            TextButton(onClick = onViewReport) {
                Text(
                    text = "View Report",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        merchants.forEach { merchant ->
            MerchantRow(merchant = merchant)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MerchantRow(merchant: MerchantSpending) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Merchant Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = merchant.name.take(3),
                fontSize = 12.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = merchant.name,
                fontSize = 15.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { merchant.percentage.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = merchant.color,
                trackColor = merchant.color.copy(alpha = 0.2f)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${formatAmount(merchant.amount)}",
                fontSize = 15.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${merchant.transactionCount} txns",
                fontSize = 11.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryBreakdownSection(categories: List<CategorySpending>) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category Breakdown",
                fontSize = 20.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(onClick = { /* Sort options */ }) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        categories.forEach { category ->
            CategoryRow(category = category)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CategoryRow(category: CategorySpending) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${category.transactionCount} Transactions",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { category.percentage.toFloat() / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = category.color,
                    trackColor = category.color.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${formatAmount(category.amount)}",
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${category.percentage}% of total",
                    fontSize = 11.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 100000 -> String.format("%.1fL", amount / 100000)
        amount >= 1000 -> String.format("%.1fk", amount / 1000)
        else -> String.format("%,.0f", amount)
    }
}

// Data classes
data class DailySpending(
    val dayLabel: String,
    val amount: Double,
    val isSelected: Boolean = false
)

data class MerchantSpending(
    val name: String,
    val amount: Double,
    val transactionCount: Int,
    val percentage: Int,
    val color: Color
)

data class CategorySpending(
    val name: String,
    val amount: Double,
    val transactionCount: Int,
    val percentage: Int,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// ViewModel
class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val budgetDao = db.budgetDao()
    
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    // Current month date range
    private val currentMonthStart: Long
    private val currentMonthEnd: Long
    
    init {
        val calendar = Calendar.getInstance()
        
        // Start of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentMonthStart = calendar.timeInMillis
        
        // End of current month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        currentMonthEnd = calendar.timeInMillis
        
        loadAnalyticsData()
        loadBudgets()
    }

    fun addBudget(amount: Double, type: BudgetType, name: String = "", bankAccountId: Long? = null, categoryId: Long? = null) {
        viewModelScope.launch {
            val budgetName = name.ifEmpty { "${type.name.lowercase().replaceFirstChar { it.uppercase() }} Budget" }
            val budget = Budget(
                name = budgetName,
                amount = amount,
                type = type,
                bankAccountId = bankAccountId,
                categoryId = categoryId
            )
            budgetDao.insert(budget)
        }
    }
    
    fun editBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.update(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.delete(budget)
        }
    }
    
    fun setBudgetFilter(type: BudgetType?) {
        _uiState.value = _uiState.value.copy(selectedBudgetFilter = type)
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            val selectedFilter = _uiState.value.selectedBudgetFilter
            val budgetFlow = if (selectedFilter != null) {
                budgetDao.getBudgetsByType(selectedFilter)
            } else {
                budgetDao.getActiveBudgets()
            }
            
            budgetFlow.collect { budgets ->
                val calendar = Calendar.getInstance()
                val currentMillis = System.currentTimeMillis()
                
                // Count budgets by type
                val dailyCount = budgets.count { it.type == BudgetType.DAILY }
                val weeklyCount = budgets.count { it.type == BudgetType.WEEKLY }
                val monthlyCount = budgets.count { it.type == BudgetType.MONTHLY }
                
                val budgetUiModels = budgets.map { budget ->
                    var start: Long = 0
                    var end: Long = 0
                    var daysTotal = 1
                    var daysRemaining = 0
                    
                    when (budget.type) {
                        BudgetType.DAILY -> {
                            val dayCal = Calendar.getInstance()
                            dayCal.set(Calendar.HOUR_OF_DAY, 0)
                            dayCal.set(Calendar.MINUTE, 0)
                            dayCal.set(Calendar.SECOND, 0)
                            dayCal.set(Calendar.MILLISECOND, 0)
                            start = dayCal.timeInMillis
                            
                            dayCal.set(Calendar.HOUR_OF_DAY, 23)
                            dayCal.set(Calendar.MINUTE, 59)
                            dayCal.set(Calendar.SECOND, 59)
                            end = dayCal.timeInMillis
                            daysTotal = 1
                            daysRemaining = 0 // Current day
                        }
                        BudgetType.WEEKLY -> {
                            val weekCal = Calendar.getInstance()
                            weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            weekCal.set(Calendar.HOUR_OF_DAY, 0)
                            weekCal.set(Calendar.MINUTE, 0)
                            weekCal.set(Calendar.SECOND, 0)
                            weekCal.set(Calendar.MILLISECOND, 0)
                            start = weekCal.timeInMillis
                            
                            weekCal.add(Calendar.DAY_OF_WEEK, 6)
                            weekCal.set(Calendar.HOUR_OF_DAY, 23)
                            weekCal.set(Calendar.MINUTE, 59)
                            weekCal.set(Calendar.SECOND, 59)
                            end = weekCal.timeInMillis
                            daysTotal = 7
                            
                            val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                            daysRemaining = if (currentDayOfWeek == Calendar.SUNDAY) 0 else (Calendar.SATURDAY - currentDayOfWeek + 1)
                        }
                        BudgetType.MONTHLY -> {
                            start = currentMonthStart
                            end = currentMonthEnd
                            daysTotal = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                            daysRemaining = daysTotal - calendar.get(Calendar.DAY_OF_MONTH)
                        }
                    }
                    
                    val spent = transactionDao.getTotalSpentSync(start, end) ?: 0.0
                    val progress = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
                    val remaining = (budget.amount - spent).coerceAtLeast(0.0)
                    val isOverBudget = spent > budget.amount
                    
                    // Calculate projected spending
                    val daysElapsed = daysTotal - daysRemaining
                    val dailyRate = if (daysElapsed > 0) spent / daysElapsed else 0.0
                    val projectedSpending = dailyRate * daysTotal
                    
                    // Status text
                    val statusText = when {
                        isOverBudget -> "Over budget!"
                        progress > 0.9f -> "Almost exceeded"
                        progress > 0.8f -> "Approaching limit"
                        projectedSpending > budget.amount && daysRemaining > 0 -> "May exceed"
                        else -> "On track"
                    }
                    
                    BudgetUiModel(
                        budget = budget,
                        spent = spent,
                        progress = progress,
                        remaining = remaining,
                        daysRemaining = daysRemaining,
                        daysTotal = daysTotal,
                        isOverBudget = isOverBudget,
                        projectedSpending = projectedSpending,
                        statusText = statusText
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    activeBudgets = budgetUiModels,
                    dailyBudgetCount = dailyCount,
                    weeklyBudgetCount = weeklyCount,
                    monthlyBudgetCount = monthlyCount
                )
            }
        }
    }
    
    private fun loadAnalyticsData() {
        viewModelScope.launch {
            // Get total spent this month
            transactionDao.getTotalSpent(currentMonthStart, currentMonthEnd).collect { spent ->
                val budget = _uiState.value.budgetAmount
                val remaining = budget - spent
                val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val dailyAvg = if (currentDay > 0) spent / currentDay else 0.0
                
                _uiState.value = _uiState.value.copy(
                    totalSpent = spent,
                    remainingBudget = remaining,
                    dailyAverage = dailyAvg
                )
            }
        }
        
        // Load all transactions for analysis
        viewModelScope.launch {
            transactionDao.getTransactionsByDateRange(currentMonthStart, currentMonthEnd).collect { transactions ->
                // Calculate weekly spending
                val weeklySpending = calculateWeeklySpending(transactions)
                
                // Calculate top merchants
                val topMerchants = calculateTopMerchants(transactions)
                
                // Calculate category breakdown
                val categoryBreakdown = calculateCategoryBreakdown(transactions)
                
                // Date range string
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val weekStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                }
                val weekEnd = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                }
                val dateRange = "${dateFormat.format(weekStart.time)} - ${dateFormat.format(weekEnd.time)}"
                
                _uiState.value = _uiState.value.copy(
                    weeklySpending = weeklySpending,
                    topMerchants = topMerchants,
                    categoryBreakdown = categoryBreakdown,
                    dateRange = dateRange
                )
            }
        }
    }
    
    private fun calculateWeeklySpending(transactions: List<com.hello.lets.test.data.entity.Transaction>): List<DailySpending> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Get start of this week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        val weeklyData = mutableListOf<DailySpending>()
        
        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val dayEnd = calendar.timeInMillis - 1
            
            val daySpent = transactions
                .filter { it.transactionType == com.hello.lets.test.data.entity.TransactionType.DEBIT }
                .filter { it.transactionDate in dayStart..dayEnd }
                .sumOf { it.amount }
            
            val dayOfWeek = Calendar.getInstance().apply { timeInMillis = dayStart }.get(Calendar.DAY_OF_WEEK)
            val isToday = dayOfWeek == today
            
            weeklyData.add(DailySpending(dayLabels[i], daySpent, isToday))
        }
        
        return weeklyData
    }
    
    private fun calculateTopMerchants(transactions: List<com.hello.lets.test.data.entity.Transaction>): List<MerchantSpending> {
        val debitTransactions = transactions.filter { 
            it.transactionType == com.hello.lets.test.data.entity.TransactionType.DEBIT 
        }
        
        val merchantGroups = debitTransactions.groupBy { it.merchant }
        val totalSpent = debitTransactions.sumOf { it.amount }
        
        val colors = listOf(
            Color(0xFFEF4444), Color(0xFFFF9800), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFF9C27B0)
        )
        
        return merchantGroups
            .map { (merchant, txns) ->
                val amount = txns.sumOf { it.amount }
                val percentage = if (totalSpent > 0) ((amount / totalSpent) * 100).toInt() else 0
                MerchantSpending(
                    name = merchant,
                    amount = amount,
                    transactionCount = txns.size,
                    percentage = percentage,
                    color = colors[merchantGroups.keys.indexOf(merchant) % colors.size]
                )
            }
            .sortedByDescending { it.amount }
            .take(5)
    }
    
    private fun calculateCategoryBreakdown(transactions: List<com.hello.lets.test.data.entity.Transaction>): List<CategorySpending> {
        val debitTransactions = transactions.filter { 
            it.transactionType == com.hello.lets.test.data.entity.TransactionType.DEBIT 
        }
        
        val totalSpent = debitTransactions.sumOf { it.amount }
        
        // Group by category - using categoryId, null means uncategorized
        val categoryGroups = debitTransactions.groupBy { it.categoryId ?: -1L }
        
        val categoryData = listOf(
            Triple("Food & Dining", Color(0xFFFF5722), Icons.Rounded.Restaurant),
            Triple("Shopping", Color(0xFF2196F3), Icons.Rounded.ShoppingBag),
            Triple("Transport", Color(0xFF9C27B0), Icons.Rounded.DirectionsCar),
            Triple("Entertainment", Color(0xFFE91E63), Icons.Rounded.Movie),
            Triple("Groceries", Color(0xFF4CAF50), Icons.Rounded.ShoppingCart),
            Triple("Uncategorized", Color(0xFF607D8B), Icons.Rounded.Category)
        )
        
        return categoryGroups
            .map { (categoryId, txns) ->
                val amount = txns.sumOf { it.amount }
                val percentage = if (totalSpent > 0) ((amount / totalSpent) * 100).toInt() else 0
                val index = if (categoryId == -1L) 5 else ((categoryId - 1) % 5).toInt()
                val (name, color, icon) = categoryData.getOrElse(index) { categoryData.last() }
                
                CategorySpending(
                    name = name,
                    amount = amount,
                    transactionCount = txns.size,
                    percentage = percentage,
                    color = color,
                    icon = icon
                )
            }
            .sortedByDescending { it.amount }
    }
    
    fun setSelectedPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        // Reload data based on period
        loadAnalyticsData()
    }
}

data class BudgetUiModel(
    val budget: Budget,
    val spent: Double,
    val progress: Float, // 0.0 to 1.0
    val remaining: Double,
    val daysRemaining: Int = 0,
    val daysTotal: Int = 1,
    val isOverBudget: Boolean = false,
    val projectedSpending: Double = 0.0,
    val statusText: String = "On track"
)

data class AnalyticsUiState(
    val totalSpent: Double = 0.0,
    val budgetAmount: Double = 50000.0,
    val remainingBudget: Double = 50000.0,
    val dailyAverage: Double = 0.0,
    val selectedPeriod: String = "Week",
    val dateRange: String = "",
    val weeklySpending: List<DailySpending> = emptyList(),
    val topMerchants: List<MerchantSpending> = emptyList(),
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val activeBudgets: List<BudgetUiModel> = emptyList(),
    val selectedBudgetFilter: BudgetType? = null, // null = All
    val dailyBudgetCount: Int = 0,
    val weeklyBudgetCount: Int = 0,
    val monthlyBudgetCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, BudgetType) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BudgetType.MONTHLY) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Budget",
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Budget Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Budget Type",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == BudgetType.DAILY,
                        onClick = { selectedType = BudgetType.DAILY },
                        label = { Text("Daily") }
                    )
                    FilterChip(
                        selected = selectedType == BudgetType.WEEKLY,
                        onClick = { selectedType = BudgetType.WEEKLY },
                        label = { Text("Weekly") }
                    )
                    FilterChip(
                        selected = selectedType == BudgetType.MONTHLY,
                        onClick = { selectedType = BudgetType.MONTHLY },
                        label = { Text("Monthly") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount, selectedType)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BudgetListSection(
    budgets: List<BudgetUiModel>,
    onEdit: (Budget) -> Unit = {},
    onDelete: (Budget) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No budgets set. Tap + to add one.",
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Active Budgets",
                fontSize = 20.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            budgets.forEach { budgetModel ->
                BudgetItemCard(
                    budgetModel = budgetModel,
                    onEdit = { onEdit(budgetModel.budget) },
                    onDelete = { onDelete(budgetModel.budget) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTypeFilterTabs(
    selectedFilter: BudgetType?,
    onFilterChange: (BudgetType?) -> Unit,
    dailyCount: Int,
    weeklyCount: Int,
    monthlyCount: Int
) {
    val filters = listOf(
        null to "All",
        BudgetType.DAILY to "Daily ($dailyCount)",
        BudgetType.WEEKLY to "Weekly ($weeklyCount)",
        BudgetType.MONTHLY to "Monthly ($monthlyCount)"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (type, label) ->
            FilterChip(
                selected = selectedFilter == type,
                onClick = { onFilterChange(type) },
                label = { 
                    Text(
                        text = label,
                        fontFamily = LiterataFontFamily,
                        fontSize = 13.sp
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun BudgetItemCard(
    budgetModel: BudgetUiModel,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit
) {
    val primaryGreen = Color(0xFF4CAF50)
    val warningOrange = Color(0xFFFF9800)
    val dangerRed = Color(0xFFEF4444)
    
    val progressColor = when {
        budgetModel.isOverBudget -> dangerRed
        budgetModel.progress > 0.8f -> warningOrange
        else -> primaryGreen
    }
    
    val statusColor = when (budgetModel.statusText) {
        "Over budget!" -> dangerRed
        "Almost exceeded", "Approaching limit" -> warningOrange
        "May exceed" -> warningOrange.copy(alpha = 0.8f)
        else -> primaryGreen
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Budget name or type
                    Text(
                        text = budgetModel.budget.name.ifEmpty { "${budgetModel.budget.type.name} Budget" },
                        fontSize = 16.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Type badge
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(progressColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = budgetModel.budget.type.name,
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = progressColor
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${String.format("%.0f", budgetModel.spent)} / ₹${String.format("%.0f", budgetModel.budget.amount)}",
                    fontSize = 18.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(budgetModel.progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { budgetModel.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Days remaining
                val daysText = when {
                    budgetModel.budget.type == BudgetType.DAILY -> "Today"
                    budgetModel.daysRemaining == 0 -> "Last day"
                    budgetModel.daysRemaining == 1 -> "1 day left"
                    else -> "${budgetModel.daysRemaining} days left"
                }
                Text(
                    text = daysText,
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = budgetModel.statusText,
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
            
            // Remaining amount
            if (budgetModel.remaining > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "₹${String.format("%.0f", budgetModel.remaining)} remaining",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = primaryGreen
                )
            }
        }
    }
}
