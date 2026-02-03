package com.hello.lets.test.screen.ui.homepage
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector

@Preview()
/*@OptIn(ExperimentalMaterial3Api::class)*/ // Needed if you use TopAppBar
@Composable
fun Homepage() {
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
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                ) {
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
                            fontSize = 14.sp, // Changed from dp to sp
                            fontFamily = LiterataFontFamily, // This looks correct!
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        )
                        Text(
                            text = "Satish",
                            fontSize = 22.sp, // Made name slightly larger (optional)
                            fontFamily = LiterataFontFamily,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, // Added Bold for emphasis
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                MyContainer()
            }
            Spacer(modifier = Modifier.height(15.dp))
            PreviewTotalSpentSection()
            Spacer(modifier = Modifier.height(25.dp))
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment =  Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp)) // Slightly rounder card
                        .background(MaterialTheme.colorScheme.primary)   // Dark Green Background from design
                        .padding(6.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "LIVE SYNC",
                    fontSize = 18.sp, // Changed from dp to sp
                    fontFamily = LiterataFontFamily, // This looks correct!
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Box (modifier = Modifier
                        .clip(RoundedCornerShape(24.dp)) // Slightly rounder card
                .background(MaterialTheme.colorScheme.surface)  // Dark Green Background from design
                        .padding(10.dp)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    RecentActivitySection()
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}



@Composable
fun MyContainer() {
    FilledTonalIconButton(
        onClick = { /* do something */ },
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
    spentAmount: Float,
    totalBudget: Float,
    modifier: Modifier = Modifier,
    chartSize: Dp = 100.dp,
    strokeWidth: Dp = 12.dp,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color
) {
    val total = totalBudget
    val spentPercentage = (spentAmount / total) * 100

    // Calculate angles:
    // If you want the "Green" (Primary) to be the big chunk and Red the small chunk:
    val sweepAngleRed = (spentPercentage / 100) * 360f
    val sweepAngleGreen = 360f - sweepAngleRed

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Draw Green Arc (Remaining)
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = sweepAngleGreen,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            )

            // 2. Draw Red Arc (Spent)
            drawArc(
                color = secondaryColor,
                startAngle = -90f + sweepAngleGreen,
                sweepAngle = sweepAngleRed,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            )
        }

        // Center Text
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
fun PreviewTotalSpentSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)) // Slightly rounder card
            .background(MaterialTheme.colorScheme.surface)   // Dark Green Background from design
            .padding(24.dp)
    ) {
        TotalSpentSection()
    }
}




@Composable
fun TotalSpentSection(
    modifier: Modifier = Modifier
) {
    // Dynamic Colors derived from Theme
    val mainTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Text Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Total Spent (Oct)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = LiterataFontFamily,
                    // Uses Theme's secondary text color (Gray or Transparent White)
                    color = secondaryTextColor
                )
                Text(
                    text = "₹45,000",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    // Uses Theme's primary text color (Black or White)
                    color = mainTextColor,
                    fontFamily = LiterataFontFamily,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "+12% vs last month",
                    fontSize = 12.sp,
                    color = secondaryTextColor,
                    fontFamily = LiterataFontFamily
                )
            }

            // Right: Donut Chart
            BudgetDonutChart(
                spentAmount = 28f,
                totalBudget = 100f,
                chartSize = 85.dp,
                strokeWidth = 10.dp,
                // Uses Theme Primary (Green) and Error (Red)
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.colorScheme.error,
                textColor = mainTextColor // Chart text adapts to theme
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(thickness = 1.dp, color = dividerColor)
        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Section
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
                    text = "₹5,000",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            OutlinedButton(
                onClick = { /* TODO: Navigate */ },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, dividerColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    // Text color matches the theme
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



// --- 1. DATA MODEL ---
data class Transaction(
    val title: String,
    val date: String,
    val amount: String,
    val category: String,
    val icon: ImageVector,
    val mainColor: Color, // Text color for amount
    val iconBgColor: Color, // Background for icon circle
    val isPositive: Boolean = false
)


@Composable
fun RecentActivitySection() {

    // 1. Capture Theme Colors
    val cardBackground = MaterialTheme.colorScheme.surface // 0xFF101A15 in Dark, White in Light
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val positiveColor = MaterialTheme.colorScheme.primary
    val negativeColor = MaterialTheme.colorScheme.error
    val widgetBackground = MaterialTheme.colorScheme.surfaceVariant

    // Icon backgrounds (Subtle tint of the main color)
    val redIconBg = negativeColor.copy(alpha = 0.1f)
    val greenIconBg = positiveColor.copy(alpha = 0.1f)

    // 2. Define Data INSIDE Composable to use Theme Colors
    val transactions = listOf(
        Transaction(
            title = "Uber Ride",
            date = "Yesterday",
            amount = "- ₹450.00",
            category = "Transport",
            icon = Icons.Rounded.AddCircle, // Make sure to use correct icon
            mainColor = negativeColor,
            iconBgColor = redIconBg
        ),
        Transaction(
            title = "Salary Oct",
            date = "Oct 30",
            amount = "+ ₹80,000.00",
            category = "Income",
            icon = Icons.Rounded.Build,
            mainColor = positiveColor,
            iconBgColor = greenIconBg,
            isPositive = true
        ),
        Transaction(
            title = "Netflix",
            date = "Oct 28",
            amount = "- ₹649.00",
            category = "Subscription",
            icon = Icons.Rounded.Home,
            mainColor = negativeColor,
            iconBgColor = redIconBg
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp), // Adjusted padding
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
            TextButton(onClick = { }) {
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = positiveColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // List
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            transactions.forEach { item ->
                ActivityItem(
                    transaction = item,
                    // Pass the card background explicitly from theme
                    backgroundColor = cardBackground,
                    titleColor = primaryText,
                    subtitleColor = secondaryText,
                    widgetBackground = widgetBackground
                )
            }
        }
    }
}

// Updated ActivityItem to accept Theme Colors
@Composable
fun ActivityItem(
    transaction: Transaction,
    backgroundColor: Color,
    titleColor: Color,
    subtitleColor: Color,
    widgetBackground: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(widgetBackground) // Uses Theme Surface color
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(transaction.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    // If positive, use Green. If negative, use Red.
                    tint = transaction.mainColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = transaction.title,
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(
                    text = transaction.date,
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = subtitleColor
                )
            }
        }

        // Amount
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = transaction.amount,
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = transaction.mainColor
            )
            Text(
                text = transaction.category,
                fontSize = 12.sp,
                fontFamily = LiterataFontFamily,
                color = subtitleColor
            )
        }
    }
}