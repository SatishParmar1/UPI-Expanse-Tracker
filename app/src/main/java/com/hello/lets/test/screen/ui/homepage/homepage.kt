package com.hello.lets.test.screen.ui.homepage
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
@Preview()
/*@OptIn(ExperimentalMaterial3Api::class)*/ // Needed if you use TopAppBar
@Composable
fun Homepage() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        // 'innerPadding' calculates the space needed for the TopBar/BottomBar
        // You MUST apply it to your root content container (Column/Box)
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
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

// --- PREVIEW ---

@Composable
fun PreviewTotalSpentSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)) // Slightly rounder card
            .background(Color(0xFF192520))   // Dark Green Background from design
            .padding(24.dp)
    ) {
        TotalSpentSection()
    }
}




@Composable
fun TotalSpentSection(
    modifier: Modifier = Modifier
) {
    // Colors
    val primaryColor = Color(0xFF39E079) // Bright Green
    val errorColor = Color(0xFFFF3D5E)    // Red
    val whiteTransparent60 = Color.White.copy(alpha = 0.6f)
    val whiteTransparent40 = Color.White.copy(alpha = 0.4f)
    val whiteTransparent10 = Color.White.copy(alpha = 0.1f)
    val dividerColor = Color.White.copy(alpha = 0.08f) // Very subtle line

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // -----------------------------------------------------
        // TOP SECTION: Text + Donut Chart (Existing)
        // -----------------------------------------------------
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
                    color = whiteTransparent60
                )
                Text(
                    text = "₹45,000",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = LiterataFontFamily,
                    letterSpacing = (-0.5).sp
                )

                // +12% Badge (Simplified for brevity, insert your previous badge code here)
                Text(
                    text = "+12% vs last month",
                    fontSize = 12.sp,
                    color = whiteTransparent40,
                    fontFamily = LiterataFontFamily
                )
            }

            // Right: Donut Chart
            BudgetDonutChart(
                spentAmount = 28f,
                totalBudget = 100f,
                chartSize = 85.dp,
                strokeWidth = 10.dp,
                primaryColor = primaryColor,
                secondaryColor = errorColor,
                textColor = Color.White
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
            // Left: Remaining Label
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "REMAINING BUDGET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LiterataFontFamily,
                    color = whiteTransparent40,
                    letterSpacing = 1.sp // Caps usually look better with spacing
                )
                Text(
                    text = "₹5,000",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = LiterataFontFamily,
                    color = primaryColor // Green color
                )
            }

            // Right: View Analytics Button
            OutlinedButton(
                onClick = { /* TODO: Navigate */ },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, whiteTransparent10), // Subtle border
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp) // Compact height
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