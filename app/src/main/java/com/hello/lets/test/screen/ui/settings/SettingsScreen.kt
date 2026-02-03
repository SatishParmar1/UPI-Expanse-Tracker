package com.hello.lets.test.screen.ui.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.ExcludedSender
import com.hello.lets.test.data.entity.ParsingRule
import com.hello.lets.test.data.repository.TransactionRepository
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Settings screen with privacy, SMS parsing, appearance, and account options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val parsingRules by viewModel.parsingRules.collectAsState()
    val excludedSenders by viewModel.excludedSenders.collectAsState()
    val activeRulesCount by viewModel.activeRulesCount.collectAsState()
    val excludedCount by viewModel.excludedCount.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Privacy & Security Section
            item {
                SectionHeader("PRIVACY & SECURITY")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Lock,
                    title = "App Lock",
                    subtitle = null,
                    trailing = {
                        var checked by remember { mutableStateOf(false) }
                        Switch(
                            checked = checked,
                            onCheckedChange = { checked = it }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Face,
                    title = "Face ID",
                    subtitle = null,
                    trailing = {
                        var checked by remember { mutableStateOf(true) }
                        Switch(
                            checked = checked,
                            onCheckedChange = { checked = it }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Refresh,
                    title = "Local Backup",
                    subtitle = "Last backup: Today, 9:00 AM",
                    onClick = { /* TODO */ }
                )
            }
            
            item {
                LocalStorageIndicator()
            }
            
            // SMS Parsing Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("SMS PARSING")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Settings,
                    title = "Keyword Rules",
                    subtitle = "$activeRulesCount custom rules active",
                    onClick = { /* TODO: Navigate to rules screen */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Clear,
                    title = "Excluded Senders",
                    subtitle = "$excludedCount blocked",
                    onClick = { /* TODO: Navigate to excluded senders */ }
                )
            }
            
            // Appearance Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("APPEARANCE")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Brightness4,
                    title = "Theme",
                    subtitle = "System Dark",
                    onClick = { /* TODO */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Palette,
                    title = "Accents",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    },
                    onClick = { /* TODO */ }
                )
            }
            
            // Account Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("ACCOUNT")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Upload,
                    title = "Export Data",
                    onClick = { /* TODO */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Rounded.Delete,
                    title = "Delete All Data",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { viewModel.deleteAllData() }
                )
            }
            
            // Version info
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Version 2.4.0 (Build 302)",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "© 2024 FinanceTracker Inc.",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontFamily = LiterataFontFamily,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LocalStorageIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
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
            text = "DATA ENCRYPTED LOCALLY",
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
    }
}

/**
 * ViewModel for Settings screen.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(
        database.transactionDao(),
        database.categoryDao(),
        database.parsingRuleDao(),
        database.excludedSenderDao()
    )
    
    val parsingRules: StateFlow<List<ParsingRule>> = repository.getAllParsingRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val excludedSenders: StateFlow<List<ExcludedSender>> = repository.getAllExcludedSenders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val activeRulesCount: StateFlow<Int> = repository.getActiveRulesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val excludedCount: StateFlow<Int> = repository.getExcludedSenderCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    fun deleteAllData() {
        viewModelScope.launch {
            // TODO: Add confirmation dialog
            // database.transactionDao().deleteAll()
        }
    }
    
    fun addParsingRule(rule: ParsingRule) {
        viewModelScope.launch {
            repository.insertParsingRule(rule)
        }
    }
    
    fun updateParsingRule(rule: ParsingRule) {
        viewModelScope.launch {
            repository.updateParsingRule(rule)
        }
    }
    
    fun deleteParsingRule(rule: ParsingRule) {
        viewModelScope.launch {
            repository.deleteParsingRule(rule)
        }
    }
    
    fun addExcludedSender(sender: ExcludedSender) {
        viewModelScope.launch {
            repository.insertExcludedSender(sender)
        }
    }
    
    fun removeExcludedSender(sender: ExcludedSender) {
        viewModelScope.launch {
            repository.deleteExcludedSender(sender)
        }
    }
}
