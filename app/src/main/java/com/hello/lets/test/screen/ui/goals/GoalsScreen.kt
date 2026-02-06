package com.hello.lets.test.screen.ui.goals

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.GoalPriority
import com.hello.lets.test.data.entity.SavingsGoal
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Goals screen showing savings goals with progress tracking.
 */

@Preview()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = viewModel(),
    onAddGoalClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val goals by viewModel.goals.collectAsState()
    
    // Colors
    val primaryGreen = Color(0xFF4CAF50)
    val darkBackground = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    
    Scaffold(
        containerColor = darkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SAVINGS",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = textSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Your Goals",
                            fontSize = 28.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddGoalClick,
                containerColor = surfaceColor,
                contentColor = textPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create New Goal",
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Saved Summary Card
            item {
                TotalSavedCard(
                    totalSaved = uiState.totalSaved,
                    totalTarget = uiState.totalTarget,
                    progressPercentage = uiState.overallProgress
                )
            }
            
            // Individual Goals
            items(goals) { goal ->
                GoalCard(
                    goal = goal,
                    onToggleSmartSave = { viewModel.toggleSmartSave(goal) },
                    onClick = { /* TODO: Navigate to goal detail */ }
                )
            }
            
            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun TotalSavedCard(
    totalSaved: Double,
    totalTarget: Double,
    progressPercentage: Float
) {
    val primaryGreen = Color(0xFF4CAF50)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradientBrush)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Total Saved across all goals",
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "₹${formatAmount(totalSaved)}",
                fontSize = 36.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { (progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = primaryGreen,
                trackColor = primaryGreen.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${progressPercentage.toInt()}% of Total Target",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Target: ₹${formatAmount(totalTarget)}",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: SavingsGoal,
    onToggleSmartSave: () -> Unit,
    onClick: () -> Unit
) {
    val primaryGreen = Color(0xFF4CAF50)
    val goalColor = try {
        Color(android.graphics.Color.parseColor(goal.colorHex))
    } catch (e: Exception) {
        primaryGreen
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Goal Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(goalColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getGoalIcon(goal.iconName),
                            contentDescription = null,
                            tint = goalColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = goal.name,
                            fontSize = 18.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        goal.description?.let {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                fontFamily = LiterataFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Priority Badge
                if (goal.priority == GoalPriority.HIGH) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "HIGH\nPRIORITY",
                            fontSize = 8.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "₹${formatAmount(goal.savedAmount)}",
                    fontSize = 28.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/ ₹${formatAmount(goal.targetAmount)}",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { (goal.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = goalColor,
                trackColor = goalColor.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${goal.progressPercentage.toInt()}% Achieved",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = goalColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Smart Save Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Save",
                        fontSize = 14.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = goal.smartSaveEnabled,
                    onCheckedChange = { onToggleSmartSave() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryGreen,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Goal Insight Card
            GoalInsightCard(goal = goal)
        }
        
        // Animated Progress Ring at top right
        AnimatedProgressRing(
            progress = goal.progressPercentage / 100f,
            color = goalColor,
            modifier = Modifier.align(Alignment.TopEnd),
            size = 60.dp,
            strokeWidth = 6.dp
        )
    }
}

fun getGoalIcon(iconName: String): ImageVector {
    return when (iconName) {
        "shield" -> Icons.Rounded.Shield
        "flight" -> Icons.Rounded.Flight
        "laptop" -> Icons.Rounded.Laptop
        "home" -> Icons.Rounded.Home
        "car" -> Icons.Rounded.DirectionsCar
        "school" -> Icons.Rounded.School
        "health" -> Icons.Rounded.LocalHospital
        else -> Icons.Rounded.Savings
    }
}

fun formatAmount(amount: Double): String {
    return when {
        amount >= 10000000 -> String.format("%.2f Cr", amount / 10000000)
        amount >= 100000 -> String.format("%.2f L", amount / 100000)
        else -> String.format("%,.0f", amount)
    }
}

@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4CAF50),
    strokeWidth: Dp = 8.dp,
    size: Dp = 80.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = 360 * animatedProgress
            val strokeWidthPx = strokeWidth.toPx()
            
            // Background track
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = Size(size.toPx() - strokeWidthPx, size.toPx() - strokeWidthPx),
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            )
            
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = Size(size.toPx() - strokeWidthPx, size.toPx() - strokeWidthPx),
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            )
        }
        
        // Percentage text
        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 14.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun GoalInsightCard(goal: SavingsGoal) {
    val primaryGreen = Color(0xFF4CAF50)
    val remaining = goal.targetAmount - goal.savedAmount
    val daysToTarget = goal.targetDate?.let {
        val daysLeft = TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis()).toInt()
        if (daysLeft > 0) daysLeft else 0
    } ?: 30
    
    val dailySavingsNeeded = if (daysToTarget > 0) remaining / daysToTarget else remaining
    
    if (remaining > 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(primaryGreen.copy(alpha = 0.1f))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Savings💡",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Save ₹${formatAmount(dailySavingsNeeded)}/day to reach your goal",
                        fontSize = 11.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (daysToTarget > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryGreen)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${daysToTarget}d left",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * ViewModel for Goals screen.
 */
class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val savingsGoalDao = db.savingsGoalDao()
    
    // Real data from database
    val goals: StateFlow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()
    
    init {
        loadTotals()
    }
    
    private fun loadTotals() {
        viewModelScope.launch {
            savingsGoalDao.getTotalSaved().collect { totalSaved ->
                _uiState.value = _uiState.value.copy(totalSaved = totalSaved)
                updateProgress()
            }
        }
        
        viewModelScope.launch {
            savingsGoalDao.getTotalTarget().collect { totalTarget ->
                _uiState.value = _uiState.value.copy(totalTarget = totalTarget)
                updateProgress()
            }
        }
    }
    
    private fun updateProgress() {
        val state = _uiState.value
        val progress = if (state.totalTarget > 0) {
            ((state.totalSaved / state.totalTarget) * 100).toFloat()
        } else 0f
        _uiState.value = state.copy(overallProgress = progress)
    }
    
    fun toggleSmartSave(goal: SavingsGoal) {
        viewModelScope.launch {
            savingsGoalDao.update(goal.copy(smartSaveEnabled = !goal.smartSaveEnabled))
        }
    }
    
    fun showAddGoalDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }
    
    fun hideAddGoalDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }
    
    fun addGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            savingsGoalDao.insert(goal)
            hideAddGoalDialog()
        }
    }
    
    fun updateGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            savingsGoalDao.update(goal)
        }
    }
    
    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            savingsGoalDao.delete(goal)
        }
    }
}

data class GoalsUiState(
    val totalSaved: Double = 0.0,
    val totalTarget: Double = 0.0,
    val overallProgress: Float = 0f,
    val showAddDialog: Boolean = false
)
