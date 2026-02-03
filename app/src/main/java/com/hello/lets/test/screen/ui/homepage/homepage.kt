package com.hello.lets.test.screen.ui.homepage

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hello.lets.test.R
import com.hello.lets.test.ui.theme.LiterataFontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.entity.Transaction
import com.hello.lets.test.data.entity.TransactionType
import com.hello.lets.test.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Homepage(viewModel: DashboardViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    
    // SMS Permission handling
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == 
                PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSmsPermission = granted
        if (granted) {
            viewModel.syncSms()
        }
    }
    
    // Auto-sync on first launch with permission
    LaunchedEffect(hasSmsPermission) {
        if (hasSmsPermission && uiState.transactionCount == 0) {
            viewModel.syncSms()
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(70.dp)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            )
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Welcome Back,",
                            fontSize = 14.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Satish",
                            fontSize = 22.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                NotificationButton()
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Local Storage Badge
            LocalStorageBadge()
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // Total Spent Section with real data
            TotalSpentCard(
                totalSpent = uiState.totalSpent,
                budget = uiState.budget,
                remainingBudget = uiState.remainingBudget,
                spentPercentage = uiState.spentPercentage
            )
            
            Spacer(modifier = Modifier.height(25.dp))
            
            // Live Sync Section
            LiveSyncSection(
                hasSmsPermission = hasSmsPermission,
                isSyncing = syncState.isSyncing,
                syncedCount = syncState.syncedCount,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_SMS) },
                onSync = { viewModel.syncSms() }
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // Recent Activity with real transactions
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    RecentActivitySection(
                        transactions = uiState.recentTransactions,
                        isLoading = uiState.isLoading
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun LocalStorageBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "LOCAL STORAGE ENCRYPTED",
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun LiveSyncSection(
    hasSmsPermission: Boolean,
    isSyncing: Boolean,
    syncedCount: Int,
    onRequestPermission: () -> Unit,
    onSync: () -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSyncing) MaterialTheme.colorScheme.tertiary 
                            else MaterialTheme.colorScheme.primary
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isSyncing) "SYNCING..." else "LIVE SYNC",
                    fontSize = 18.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            
            if (!hasSmsPermission) {
                TextButton(onClick = onRequestPermission) {
                    Text(
                        text = "Grant SMS Access",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                IconButton(
                    onClick = onSync,
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        if (syncedCount > 0) {
            Text(
                text = "Found $syncedCount new transactions",
                fontSize = 12.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun NotificationButton() {
    FilledTonalIconButton(
        onClick = { /* TODO */ },
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape
            ),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.Notifications,
            contentDescription = "Notifications"
        )
    }
}

@Composable
fun BudgetDonutChart(
    spentPercentage: Float,
    modifier: Modifier = Modifier,
    chartSize: Dp = 100.dp,
    strokeWidth: Dp = 12.dp,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color
) {
    val sweepAngleRed = (spentPercentage / 100) * 360f
    val sweepAngleGreen = 360f - sweepAngleRed

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = sweepAngleGreen,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            )
            drawArc(
                color = secondaryColor,
                startAngle = -90f + sweepAngleGreen,
                sweepAngle = sweepAngleRed,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "BUDGET",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 8.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${spentPercentage.toInt()}%",
                color = textColor,
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TotalSpentCard(
    totalSpent: Double,
    budget: Double,
    remainingBudget: Double,
    spentPercentage: Float
) {
    val mainTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Total Spent (This Month)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = LiterataFontFamily,
                        color = secondaryTextColor
                    )
                    Text(
                        text = formatCurrency(totalSpent),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = mainTextColor,
                        fontFamily = LiterataFontFamily,
                        letterSpacing = (-0.5).sp
                    )
                }

                BudgetDonutChart(
                    spentPercentage = spentPercentage.coerceIn(0f, 100f),
                    chartSize = 85.dp,
                    strokeWidth = 10.dp,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.error,
                    textColor = mainTextColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 1.dp, color = dividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "REMAINING BUDGET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = LiterataFontFamily,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatCurrency(remainingBudget.coerceAtLeast(0.0)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = LiterataFontFamily,
                        color = if (remainingBudget >= 0) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.error,
                    )
                }

                OutlinedButton(
                    onClick = { /* TODO: Navigate to Analytics */ },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, dividerColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = mainTextColor
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "View Analytics",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    transactions: List<Transaction>,
    isLoading: Boolean
) {
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val positiveColor = MaterialTheme.colorScheme.primary
    val negativeColor = MaterialTheme.colorScheme.error
    val cardBackground = MaterialTheme.colorScheme.surface
    val widgetBackground = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                fontSize = 18.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            TextButton(onClick = { /* TODO: Navigate to all transactions */ }) {
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = positiveColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = secondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No transactions yet",
                        fontSize = 14.sp,
                        fontFamily = LiterataFontFamily,
                        color = secondaryText
                    )
                    Text(
                        text = "Sync your SMS to get started",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        color = secondaryText.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                transactions.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        backgroundColor = cardBackground,
                        titleColor = primaryText,
                        subtitleColor = secondaryText,
                        widgetBackground = widgetBackground,
                        positiveColor = positiveColor,
                        negativeColor = negativeColor
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    backgroundColor: Color,
    titleColor: Color,
    subtitleColor: Color,
    widgetBackground: Color,
    positiveColor: Color,
    negativeColor: Color
) {
    val isCredit = transaction.transactionType == TransactionType.CREDIT
    val amountColor = if (isCredit) positiveColor else negativeColor
    val amountPrefix = if (isCredit) "+ " else "- "
    val iconBgColor = amountColor.copy(alpha = 0.1f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(widgetBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.categoryId),
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = transaction.merchant,
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1
                )
                Text(
                    text = formatDate(transaction.transactionDate),
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = subtitleColor
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$amountPrefix${formatCurrency(transaction.amount)}",
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            Text(
                text = if (isCredit) "Income" else "Expense",
                fontSize = 12.sp,
                fontFamily = LiterataFontFamily,
                color = subtitleColor
            )
        }
    }
}

// Helper functions
fun formatCurrency(amount: Double): String {
    return "₹${String.format(Locale.US, "%,.2f", amount)}"
}

fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 24 * 60 * 60 * 1000 -> "Today"
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

fun getCategoryIcon(categoryId: Long?): ImageVector {
    return when (categoryId) {
        1L -> Icons.Rounded.Star // Food
        2L -> Icons.Rounded.Star // Transport
        3L -> Icons.Rounded.Star // Entertainment
        4L -> Icons.Rounded.ShoppingCart // Groceries
        5L -> Icons.Rounded.ShoppingCart // Shopping
        else -> Icons.Rounded.Star
    }
}