package com.hello.lets.test.screen.ui.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import org.json.JSONObject

/**
 * Settings screen with privacy, SMS parsing, appearance, and account options.
 */
@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateToKeywordRules: () -> Unit = {},
    onNavigateToExcludedSenders: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onExportData: () -> Unit = {},
    onDeleteAllData: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    var searchQuery by remember { mutableStateOf("") }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.theme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { mode ->
                viewModel.setTheme(mode)
                showThemeDialog = false
            }
        )
    }
    
    if (showAccentDialog) {
        AccentColorDialog(
            onDismiss = { showAccentDialog = false },
            onColorSelected = { colorHex ->
                viewModel.setAccentColor(colorHex)
                showAccentDialog = false
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data?", fontFamily = LiterataFontFamily) },
            text = { Text("This action cannot be undone. All transactions, goals, and settings will be permanently deleted.", fontFamily = LiterataFontFamily) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                        android.widget.Toast.makeText(context, "All data deleted", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete Forever", fontFamily = LiterataFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", fontFamily = LiterataFontFamily)
                }
            }
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 28.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(surfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search settings...",
                            fontFamily = LiterataFontFamily
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = surfaceColor,
                        focusedContainerColor = surfaceColor,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = primaryGreen
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Privacy & Security Section
            item {
                SectionHeader("PRIVACY & SECURITY")
            }
            
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.Lock,
                    title = "App Lock",
                    subtitle = null,
                    isChecked = uiState.isAppLockEnabled,
                    onCheckedChange = { viewModel.toggleAppLock() }
                )
            }
            
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.Fingerprint,
                    title = "Face ID",
                    subtitle = null,
                    isChecked = uiState.isBiometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometric() }
                )
            }
            
            item {
                SettingsNavigationItem(
                    icon = Icons.Rounded.Backup,
                    title = "Local Backup",
                    subtitle = "Last backup: ${uiState.lastBackupTime}",
                    onClick = onNavigateToBackup
                )
            }
            
            item {
                EncryptionBadge()
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // SMS Parsing Section
            item {
                SectionHeader("SMS PARSING")
            }
            
            item {
                SettingsNavigationItem(
                    icon = Icons.Rounded.Rule,
                    title = "Keyword Rules",
                    subtitle = "${uiState.activeRulesCount} custom rules active",
                    onClick = onNavigateToKeywordRules
                )
            }
            
            item {
                SettingsNavigationItem(
                    icon = Icons.Rounded.Block,
                    title = "Excluded Senders",
                    subtitle = "${uiState.excludedSendersCount} blocked",
                    onClick = onNavigateToExcludedSenders
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Appearance Section
            item {
                SectionHeader("APPEARANCE")
            }
            
            item {
                SettingsNavigationItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Theme",
                    value = uiState.theme,
                    onClick = { showThemeDialog = true }
                )
            }
            
            item {
                SettingsAccentItem(
                    icon = Icons.Rounded.Palette,
                    title = "Accents",
                    selectedColor = uiState.accentColor,
                    onClick = { showAccentDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Account Section
            item {
                SectionHeader("ACCOUNT")
            }
            
            item {
                SettingsNavigationItem(
                    icon = Icons.Rounded.Upload,
                    title = "Export Data",
                    subtitle = null,
                    onClick = {
                        coroutineScope.launch {
                            val json = viewModel.generateExportData()
                            val sendIntent: android.content.Intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, json)
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "Export Data")
                            context.startActivity(shareIntent)
                        }
                    }
                )
            }
            
            item {
                SettingsDangerItem(
                    icon = Icons.Rounded.DeleteForever,
                    title = "Delete All Data",
                    onClick = { showDeleteDialog = true }
                )
            }
            
            // Version Info
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Version 2.4.0 (Build 302)",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "© 2024 Tractal Inc.",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontFamily = LiterataFontFamily,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryGreen = Color(0xFF4CAF50)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
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
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryGreen = Color(0xFF4CAF50)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
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
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsAccentItem(
    icon: ImageVector,
    title: String,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryGreen = Color(0xFF4CAF50)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsDangerItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val dangerColor = Color(0xFFEF4444)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(dangerColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = dangerColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = dangerColor
                )
            }
            
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = dangerColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EncryptionBadge() {
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
            text = "DATA ENCRYPTED LOCALLY",
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = primaryGreen,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme", fontFamily = LiterataFontFamily) },
        text = {
            Column {
                ThemeOption("System Default", currentTheme == "System Default") { onThemeSelected(0) }
                ThemeOption("Light", currentTheme == "Light") { onThemeSelected(1) }
                ThemeOption("Dark", currentTheme == "Dark") { onThemeSelected(2) }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = LiterataFontFamily)
            }
        }
    )
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontFamily = LiterataFontFamily)
    }
}

@Composable
fun AccentColorDialog(
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#4CAF50" to "Green",
        "#2196F3" to "Blue",
        "#FF9800" to "Orange",
        "#E91E63" to "Pink",
        "#9C27B0" to "Purple",
        "#F44336" to "Red"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accent Color", fontFamily = LiterataFontFamily) },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colors.forEach { (hex, _) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .clickable { onColorSelected(hex) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = LiterataFontFamily)
            }
        }
    )
}

// ViewModel
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val prefs = com.hello.lets.test.data.preferences.AppPreferences.getInstance(application)
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        // Load lock settings from preferences
        _uiState.value = _uiState.value.copy(
            isAppLockEnabled = prefs.isAppLockEnabled,
            isBiometricEnabled = prefs.isBiometricEnabled
        )
        
        viewModelScope.launch {
            // Load from shared preferences and database
            db.parsingRuleDao().getActiveCount().collect { count ->
                _uiState.value = _uiState.value.copy(activeRulesCount = count)
            }
        }
        
        viewModelScope.launch {
            db.excludedSenderDao().getCount().collect { count ->
                _uiState.value = _uiState.value.copy(excludedSendersCount = count)
            }
        }
    }
    
    fun toggleAppLock() {
        val newValue = !_uiState.value.isAppLockEnabled
        prefs.isAppLockEnabled = newValue
        _uiState.value = _uiState.value.copy(isAppLockEnabled = newValue)
    }
    
    fun toggleBiometric() {
        val newValue = !_uiState.value.isBiometricEnabled
        prefs.isBiometricEnabled = newValue
        _uiState.value = _uiState.value.copy(isBiometricEnabled = newValue)
        if (newValue) {
            // Also enable app lock if biometric is enabled
            prefs.isAppLockEnabled = true
            _uiState.value = _uiState.value.copy(isAppLockEnabled = true)
        }
    }
    
    fun setTheme(mode: Int) {
        prefs.themeMode = mode
        val themeName = when (mode) {
            0 -> "System Default"
            1 -> "Light"
            2 -> "Dark"
            else -> "System Default"
        }
        _uiState.value = _uiState.value.copy(theme = themeName)
    }
    
    fun setAccentColor(colorHex: String) {
        prefs.accentColor = colorHex
        _uiState.value = _uiState.value.copy(accentColor = Color(android.graphics.Color.parseColor(colorHex)))
    }
    
    fun deleteAllData() {
        viewModelScope.launch {
            db.clearAllTables()
            // Preferences clear handled carefully to keep first run
            prefs.clear()
            loadSettings()
        }
    }
    
    suspend fun generateExportData(): String {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            val transactions = db.transactionDao().getAllTransactions().first()
            val categories = db.categoryDao().getAllCategories().first()
            val accounts = db.bankAccountDao().getAllAccounts().first()
            
            val json = JSONObject()
            json.put("exported_at", System.currentTimeMillis())
            json.put("version", 1)
            
            // Serialize transactions
            val txArray = org.json.JSONArray()
            transactions.forEach { tx ->
                val txObj = JSONObject()
                txObj.put("id", tx.id)
                txObj.put("amount", tx.amount)
                txObj.put("merchant", tx.merchant)
                txObj.put("date", tx.transactionDate)
                txObj.put("type", tx.transactionType.name)
                txArray.put(txObj)
            }
            json.put("transactions", txArray)
            json.put("categories_count", categories.size)
            json.put("accounts_count", accounts.size)
            
            // Update last backup time
            prefs.lastBackupTime = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(lastBackupTime = "Just now")
            
            json.toString(2)
        }
    }
}

data class SettingsUiState(
    val isAppLockEnabled: Boolean = true,
    val isBiometricEnabled: Boolean = true,
    val lastBackupTime: String = "Today, 9:00 AM",
    val activeRulesCount: Int = 12,
    val excludedSendersCount: Int = 3,
    val theme: String = "System Dark",
    val accentColor: Color = Color(0xFF4CAF50)
)
