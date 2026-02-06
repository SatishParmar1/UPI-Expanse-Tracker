package com.hello.lets.test.screen.ui.insights

import android.app.Application
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.TransactionType
import com.hello.lets.test.data.preferences.AppPreferences
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Insights screen with smart financial analysis and spending patterns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SMART INSIGHTS",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Financial Analysis",
                            fontSize = 24.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                InsightsSummaryCard(
                    totalSpentThisMonth = uiState.totalSpentThisMonth,
                    totalSpentLastMonth = uiState.totalSpentLastMonth,
                    monthlyChange = uiState.monthlyChange
                )
            }
            
            // Insights List
            item {
                Text(
                    text = "Your Insights",
                    fontSize = 20.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(uiState.insights) { insight ->
                InsightCard(insight = insight)
            }
            
            // Weekly Summary
            item {
                Spacer(modifier = Modifier.height(8.dp))
                WeeklySummaryCard(
                    weeklySpending = uiState.weeklySpending,
                    topCategory = uiState.topCategory,
                    transactionCount = uiState.weeklyTransactionCount
                )
            }
            
            // Tips Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saving Tips",
                    fontSize = 20.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            items(uiState.savingTips) { tip ->
                SavingTipCard(tip = tip)
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun InsightsSummaryCard(
    totalSpentThisMonth: Double,
    totalSpentLastMonth: Double,
    monthlyChange: Float
) {
    val primaryGreen = Color(0xFF4CAF50)
    val dangerRed = Color(0xFFEF4444)
    val changeColor = if (monthlyChange <= 0) primaryGreen else dangerRed
    val changeIcon = if (monthlyChange <= 0) Icons.Rounded.TrendingDown else Icons.Rounded.TrendingUp
    val changeText = if (monthlyChange <= 0) "Less than last month" else "More than last month"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "This Month's Overview",
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "₹${String.format("%,.0f", totalSpentThisMonth)}",
                fontSize = 36.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = changeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", kotlin.math.abs(monthlyChange))}%",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                
                Text(
                    text = changeText,
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun InsightCard(insight: Insight) {
    val iconColor = when (insight.type) {
        InsightType.ALERT -> Color(0xFFEF4444)
        InsightType.TIP -> Color(0xFF4CAF50)
        InsightType.INFO -> Color(0xFF2196F3)
        InsightType.WARNING -> Color(0xFFFF9800)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .animateContentSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = insight.description,
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                
                if (insight.actionText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = insight.actionText,
                        fontSize = 13.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = iconColor
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryCard(
    weeklySpending: Double,
    topCategory: String,
    transactionCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "This Week",
                fontSize = 18.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeeklyStatItem(
                    label = "Spent",
                    value = "₹${String.format("%,.0f", weeklySpending)}",
                    icon = Icons.Rounded.Payments
                )
                
                WeeklyStatItem(
                    label = "Top Category",
                    value = topCategory,
                    icon = Icons.Rounded.Category
                )
                
                WeeklyStatItem(
                    label = "Transactions",
                    value = transactionCount.toString(),
                    icon = Icons.Rounded.Receipt
                )
            }
        }
    }
}

@Composable
fun WeeklyStatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SavingTipCard(tip: SavingTip) {
    val primaryGreen = Color(0xFF4CAF50)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(primaryGreen.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = primaryGreen,
                modifier = Modifier.size(20.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tip.description,
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (tip.potentialSavings > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryGreen)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", tip.potentialSavings)}/mo",
                        fontSize = 11.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Data classes
enum class InsightType { ALERT, TIP, INFO, WARNING }

data class Insight(
    val title: String,
    val description: String,
    val type: InsightType,
    val icon: ImageVector,
    val actionText: String? = null
)

data class SavingTip(
    val title: String,
    val description: String,
    val potentialSavings: Double = 0.0
)

data class InsightsUiState(
    val totalSpentThisMonth: Double = 0.0,
    val totalSpentLastMonth: Double = 0.0,
    val monthlyChange: Float = 0f,
    val insights: List<Insight> = emptyList(),
    val weeklySpending: Double = 0.0,
    val topCategory: String = "General",
    val weeklyTransactionCount: Int = 0,
    val savingTips: List<SavingTip> = emptyList()
)

// ViewModel
class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val appPreferences = AppPreferences.getInstance(application)
    
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()
    
    init {
        loadInsights()
    }
    
    private fun loadInsights() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            
            // This month start/end
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val thisMonthStart = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val thisMonthEnd = calendar.timeInMillis
            
            // Last month start/end
            calendar.add(Calendar.MILLISECOND, 1)
            calendar.add(Calendar.MONTH, -2)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val lastMonthStart = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val lastMonthEnd = calendar.timeInMillis
            
            // This week start
            val weekCalendar = Calendar.getInstance()
            weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            weekCalendar.set(Calendar.HOUR_OF_DAY, 0)
            weekCalendar.set(Calendar.MINUTE, 0)
            weekCalendar.set(Calendar.SECOND, 0)
            val weekStart = weekCalendar.timeInMillis
            val weekEnd = System.currentTimeMillis()
            
            // Fetch data
            val thisMonthSpent = transactionDao.getTotalSpentSync(thisMonthStart, thisMonthEnd) ?: 0.0
            val lastMonthSpent = transactionDao.getTotalSpentSync(lastMonthStart, lastMonthEnd) ?: 0.0
            val weeklySpent = transactionDao.getTotalSpentSync(weekStart, weekEnd) ?: 0.0
            
            // Calculate change
            val change = if (lastMonthSpent > 0) {
                ((thisMonthSpent - lastMonthSpent) / lastMonthSpent * 100).toFloat()
            } else 0f
            
            // Generate insights based on data
            val insights = mutableListOf<Insight>()
            
            // Monthly comparison insight
            if (change > 20) {
                insights.add(Insight(
                    title = "Spending Up This Month",
                    description = "You've spent ${String.format("%.0f", change)}% more than last month. Consider reviewing your expenses.",
                    type = InsightType.WARNING,
                    icon = Icons.Rounded.TrendingUp,
                    actionText = "View spending breakdown →"
                ))
            } else if (change < -10) {
                insights.add(Insight(
                    title = "Great Job Saving!",
                    description = "You've reduced spending by ${String.format("%.0f", kotlin.math.abs(change))}% compared to last month. Keep it up!",
                    type = InsightType.TIP,
                    icon = Icons.Rounded.ThumbUp
                ))
            }
            
            // Add more insights
            insights.add(Insight(
                title = "Weekly Summary Available",
                description = "You made transactions worth ₹${String.format("%,.0f", weeklySpent)} this week.",
                type = InsightType.INFO,
                icon = Icons.Rounded.CalendarToday
            ))
            
            val monthlyBudget = appPreferences.monthlyBudget.toDouble()
            if (thisMonthSpent > monthlyBudget) {
                insights.add(Insight(
                    title = "High Spending Alert",
                    description = "You've crossed your monthly budget of ₹${String.format("%,.0f", monthlyBudget)}. Consider reviewing your expenses.",
                    type = InsightType.ALERT,
                    icon = Icons.Rounded.Warning,
                    actionText = "Review spending →"
                ))
            }
            
            // Saving tips
            val tips = listOf(
                SavingTip(
                    title = "Track Daily Expenses",
                    description = "Review your spending once a week to stay on track",
                    potentialSavings = 2000.0
                ),
                SavingTip(
                    title = "Set Category Budgets",
                    description = "Limit spending on entertainment and dining",
                    potentialSavings = 3000.0
                ),
                SavingTip(
                    title = "Use UPI for Cashback",
                    description = "Many UPI payments offer cashback rewards"
                )
            )
            
            _uiState.value = InsightsUiState(
                totalSpentThisMonth = thisMonthSpent,
                totalSpentLastMonth = lastMonthSpent,
                monthlyChange = change,
                insights = insights,
                weeklySpending = weeklySpent,
                topCategory = "Transfers", // Could be calculated from transactions
                weeklyTransactionCount = 12, // Could be calculated
                savingTips = tips
            )
        }
    }
}
