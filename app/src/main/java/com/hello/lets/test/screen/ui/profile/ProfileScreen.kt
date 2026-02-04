package com.hello.lets.test.screen.ui.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.BankAccount
import com.hello.lets.test.data.entity.BankCodes
import com.hello.lets.test.data.entity.UserProfile
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Profile screen showing user details and bank accounts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit,
    onEditProfile: () -> Unit = {},
    onAddBank: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val accentGreen = Color(0xFF00E676)
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PROFILE",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Your Details",
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
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ),
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
                    IconButton(onClick = onEditProfile) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Profile",
                            tint = primaryGreen
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
            // Profile Card
            item {
                ProfileCard(
                    profile = profile,
                    bankAccountCount = bankAccounts.size
                )
            }
            
            // Total Balance Card
            item {
                TotalBalanceCard(
                    totalBalance = totalBalance,
                    accountCount = bankAccounts.size
                )
            }
            
            // Bank Accounts Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BANK ACCOUNTS",
                        fontSize = 11.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = onAddBank) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            color = primaryGreen,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            if (bankAccounts.isEmpty()) {
                item {
                    EmptyBankAccountsCard(onAddBank = onAddBank)
                }
            } else {
                items(bankAccounts) { account ->
                    BankAccountCard(
                        account = account,
                        onClick = { /* Navigate to bank details */ }
                    )
                }
            }
            
            // Account Statistics
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "STATISTICS",
                    fontSize = 11.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            
            item {
                StatisticsCard(uiState = uiState)
            }
            
            // Privacy Badge
            item {
                PrivacyBadge()
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: UserProfile?,
    bankAccountCount: Int
) {
    val primaryGreen = Color(0xFF4CAF50)
    val accentGreen = Color(0xFF00E676)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryGreen.copy(alpha = 0.15f),
                        accentGreen.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(primaryGreen, accentGreen)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile?.name?.take(2)?.uppercase() ?: "U",
                    fontSize = 24.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile?.name ?: "User",
                    fontSize = 22.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (profile?.phoneNumber != null) {
                    Text(
                        text = profile.phoneNumber,
                        fontSize = 14.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$bankAccountCount bank${if (bankAccountCount != 1) "s" else ""} linked",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = primaryGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalBalanceCard(
    totalBalance: Double,
    accountCount: Int
) {
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Balance",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "₹${String.format(Locale.US, "%,.2f", totalBalance)}",
                fontSize = 32.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Across $accountCount account${if (accountCount != 1) "s" else ""}",
                fontSize = 13.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BankAccountCard(
    account: BankAccount,
    onClick: () -> Unit
) {
    val bankColor = try {
        Color(android.graphics.Color.parseColor(account.colorHex))
    } catch (e: Exception) {
        Color(0xFF1A73E8)
    }
    val surfaceColor = MaterialTheme.colorScheme.surface
    
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
            // Bank Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bankColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.bankCode.take(2),
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = bankColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.bankName,
                        fontSize = 16.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (account.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DEFAULT",
                                fontSize = 9.sp,
                                fontFamily = LiterataFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                
                if (account.accountNumber != null) {
                    Text(
                        text = "••••${account.accountNumber}",
                        fontSize = 13.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Balance
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.US, "%,.0f", account.currentBalance)}",
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatLastUpdated(account.lastUpdated),
                    fontSize = 11.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyBankAccountsCard(onAddBank: () -> Unit) {
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable { onAddBank() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.AccountBalance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No bank accounts linked",
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add a bank to track balances",
                fontSize = 13.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onAddBank) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = primaryGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Bank Account",
                    color = primaryGreen,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(uiState: ProfileUiState) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Column {
            StatRow("Total Transactions", "${uiState.totalTransactions}")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            StatRow("Total Spent", "₹${String.format(Locale.US, "%,.0f", uiState.totalSpent)}")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            StatRow("Total Received", "₹${String.format(Locale.US, "%,.0f", uiState.totalReceived)}")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            StatRow("Member Since", uiState.memberSince)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PrivacyBadge() {
    val primaryGreen = Color(0xFF4CAF50)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Shield,
            contentDescription = null,
            tint = primaryGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "ALL DATA STORED LOCALLY",
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = primaryGreen,
            letterSpacing = 1.sp
        )
    }
}

private fun formatLastUpdated(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> {
            val format = SimpleDateFormat("dd MMM", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
}

// Data classes
data class ProfileUiState(
    val totalTransactions: Int = 0,
    val totalSpent: Double = 0.0,
    val totalReceived: Double = 0.0,
    val memberSince: String = "Today"
)

// ViewModel
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val userProfileDao = db.userProfileDao()
    private val bankAccountDao = db.bankAccountDao()
    private val transactionDao = db.transactionDao()
    
    val profile: StateFlow<UserProfile?> = userProfileDao.getProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    val bankAccounts: StateFlow<List<BankAccount>> = bankAccountDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val totalBalance: StateFlow<Double> = bankAccountDao.getTotalBalance()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            // Load total transactions
            transactionDao.getAllTransactions().collect { transactions ->
                val spent = transactions.filter { 
                    it.transactionType == com.hello.lets.test.data.entity.TransactionType.DEBIT 
                }.sumOf { it.amount }
                
                val received = transactions.filter { 
                    it.transactionType == com.hello.lets.test.data.entity.TransactionType.CREDIT 
                }.sumOf { it.amount }
                
                val memberSince = profile.value?.createdAt?.let {
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it))
                } ?: "Today"
                
                _uiState.value = ProfileUiState(
                    totalTransactions = transactions.size,
                    totalSpent = spent,
                    totalReceived = received,
                    memberSince = memberSince
                )
            }
        }
    }
}
