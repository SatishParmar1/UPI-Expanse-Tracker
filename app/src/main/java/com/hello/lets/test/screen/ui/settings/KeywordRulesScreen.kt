package com.hello.lets.test.screen.ui.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.ParsingRule
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Parsing Rules screen for managing SMS parsing rules.
 * Includes test configuration and keyword → category mappings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordRulesScreen(
    viewModel: KeywordRulesViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val rules by viewModel.rules.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    var showAddDialog by remember { mutableStateOf(false) }
    var testSmsText by remember { mutableStateOf("") }
    var parseResult by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Parsing Rules",
                        fontSize = 22.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
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
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(primaryGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Rule",
                            tint = Color.White
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
            // Test Configuration Section
            item {
                Text(
                    text = "TEST CONFIGURATION",
                    fontSize = 11.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            }
            
            item {
                TestConfigurationCard(
                    testSmsText = testSmsText,
                    onTextChange = { testSmsText = it },
                    parseResult = parseResult,
                    onParse = {
                        parseResult = viewModel.testParseSms(testSmsText)
                    }
                )
            }
            
            // Active Rules Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Rules",
                        fontSize = 20.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${rules.size} Rules",
                            fontSize = 14.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Manage",
                            fontSize = 14.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = primaryGreen,
                            modifier = Modifier.clickable { /* Toggle edit mode */ }
                        )
                    }
                }
            }
            
            // Rules List
            items(rules) { rule ->
                ParseRuleCard(
                    rule = rule,
                    onDelete = { viewModel.deleteRule(rule) }
                )
            }
            
            // Sample Rules if empty
            if (rules.isEmpty()) {
                item {
                    SampleRulesSection(
                        onAddSampleRules = { viewModel.addSampleRules() }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
    
    // Add Rule Dialog
    if (showAddDialog) {
        AddParseRuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { categoryName, keywords, icon ->
                viewModel.addRuleWithKeywords(categoryName, keywords, icon)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TestConfigurationCard(
    testSmsText: String,
    onTextChange: (String) -> Unit,
    parseResult: String?,
    onParse: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryGreen = Color(0xFF4CAF50)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Column {
            // Input Area
            Text(
                text = "PASTE SMS TO TEST",
                fontSize = 10.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = testSmsText,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    placeholder = {
                        Text(
                            text = "e.g. Acct XX89 debited for Rs 350.00 on 22-Oct at Swiggy...",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 13.sp,
                        fontFamily = LiterataFontFamily
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onParse,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "PARSE",
                        fontSize = 12.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Result Area
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = parseResult ?: "Result will appear here...",
                    fontSize = 13.sp,
                    fontFamily = LiterataFontFamily,
                    color = if (parseResult != null) primaryGreen 
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ParseRuleCard(
    rule: ParsingRule,
    onDelete: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryGreen = Color(0xFF4CAF50)
    
    // Determine icon based on category
    val categoryIcon = when {
        rule.extractedCategory?.contains("Food", ignoreCase = true) == true -> Icons.Rounded.Restaurant
        rule.extractedCategory?.contains("Ride", ignoreCase = true) == true -> Icons.Rounded.DirectionsCar
        rule.extractedCategory?.contains("Entertainment", ignoreCase = true) == true -> Icons.Rounded.Movie
        rule.extractedCategory?.contains("Grocery", ignoreCase = true) == true -> Icons.Rounded.ShoppingCart
        rule.extractedCategory?.contains("Shopping", ignoreCase = true) == true -> Icons.Rounded.ShoppingBag
        else -> Icons.Rounded.Category
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Keyword Box
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, primaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TextFields,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "KEYWORD",
                    fontSize = 8.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Arrow
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = primaryGreen,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Category Icon Box
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CATEGORY",
                    fontSize = 8.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Category Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.extractedCategory ?: "Uncategorized",
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "\"${rule.keyword}\"",
                    fontSize = 12.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SampleRulesSection(onAddSampleRules: () -> Unit) {
    val primaryGreen = Color(0xFF4CAF50)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No rules configured yet",
            fontSize = 16.sp,
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onAddSampleRules,
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
        ) {
            Text("Add Sample Rules", fontFamily = LiterataFontFamily)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddParseRuleDialog(
    onDismiss: () -> Unit,
    onSave: (String, List<String>, String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("restaurant") }
    
    val primaryGreen = Color(0xFF4CAF50)
    
    val iconOptions = listOf(
        "restaurant" to Icons.Rounded.Restaurant,
        "car" to Icons.Rounded.DirectionsCar,
        "movie" to Icons.Rounded.Movie,
        "shopping" to Icons.Rounded.ShoppingBag,
        "grocery" to Icons.Rounded.ShoppingCart,
        "health" to Icons.Rounded.LocalHospital,
        "utilities" to Icons.Rounded.ElectricBolt,
        "travel" to Icons.Rounded.Flight
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Parsing Rule",
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name", fontFamily = LiterataFontFamily) },
                    placeholder = { Text("e.g., Food Apps", fontFamily = LiterataFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("Keywords (comma separated)", fontFamily = LiterataFontFamily) },
                    placeholder = { Text("Swiggy, Zomato, DoorDash", fontFamily = LiterataFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "Select Icon",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.take(4).forEach { (id, icon) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedIcon == id) primaryGreen.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (selectedIcon == id) 2.dp else 1.dp,
                                    color = if (selectedIcon == id) primaryGreen 
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = id },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = id,
                                tint = if (selectedIcon == id) primaryGreen 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.drop(4).forEach { (id, icon) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedIcon == id) primaryGreen.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (selectedIcon == id) 2.dp else 1.dp,
                                    color = if (selectedIcon == id) primaryGreen 
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = id },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = id,
                                tint = if (selectedIcon == id) primaryGreen 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank() && keywords.isNotBlank()) {
                        val keywordList = keywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onSave(categoryName, keywordList, selectedIcon)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("Save", fontFamily = LiterataFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = LiterataFontFamily)
            }
        }
    )
}

// ViewModel
class KeywordRulesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    
    private val _uiState = MutableStateFlow(KeywordRulesUiState())
    val uiState: StateFlow<KeywordRulesUiState> = _uiState.asStateFlow()
    
    private val _rules = MutableStateFlow<List<ParsingRule>>(emptyList())
    val rules: StateFlow<List<ParsingRule>> = _rules.asStateFlow()
    
    init {
        loadRules()
    }
    
    private fun loadRules() {
        viewModelScope.launch {
            db.parsingRuleDao().getAllRules().collect { ruleList ->
                _rules.value = ruleList
            }
        }
    }
    
    fun addRuleWithKeywords(categoryName: String, keywords: List<String>, icon: String) {
        viewModelScope.launch {
            keywords.forEach { keyword ->
                val rule = ParsingRule(
                    keyword = keyword,
                    pattern = ".*${keyword.replace(" ", "\\s+")}.*",
                    extractedCategory = categoryName,
                    priority = 3,
                    isActive = true
                )
                db.parsingRuleDao().insert(rule)
            }
        }
    }
    
    fun deleteRule(rule: ParsingRule) {
        viewModelScope.launch {
            db.parsingRuleDao().delete(rule)
        }
    }
    
    fun addSampleRules() {
        viewModelScope.launch {
            val sampleRules = listOf(
                ParsingRule(keyword = "Swiggy", pattern = ".*Swiggy.*", extractedCategory = "Food Apps", priority = 3, isActive = true),
                ParsingRule(keyword = "Zomato", pattern = ".*Zomato.*", extractedCategory = "Food Apps", priority = 3, isActive = true),
                ParsingRule(keyword = "Uber", pattern = ".*Uber.*", extractedCategory = "Ride Sharing", priority = 3, isActive = true),
                ParsingRule(keyword = "Ola", pattern = ".*Ola.*", extractedCategory = "Ride Sharing", priority = 3, isActive = true),
                ParsingRule(keyword = "Netflix", pattern = ".*Netflix.*", extractedCategory = "Entertainment", priority = 3, isActive = true),
                ParsingRule(keyword = "Spotify", pattern = ".*Spotify.*", extractedCategory = "Entertainment", priority = 3, isActive = true),
                ParsingRule(keyword = "Blinkit", pattern = ".*Blinkit.*", extractedCategory = "Groceries", priority = 3, isActive = true),
                ParsingRule(keyword = "Zepto", pattern = ".*Zepto.*", extractedCategory = "Groceries", priority = 3, isActive = true),
                ParsingRule(keyword = "Amazon", pattern = ".*Amazon.*", extractedCategory = "Shopping", priority = 3, isActive = true),
                ParsingRule(keyword = "Flipkart", pattern = ".*Flipkart.*", extractedCategory = "Shopping", priority = 3, isActive = true)
            )
            sampleRules.forEach { rule ->
                db.parsingRuleDao().insert(rule)
            }
        }
    }
    
    fun testParseSms(smsText: String): String {
        if (smsText.isBlank()) return "Enter SMS text to test"
        
        // Simple parsing test
        val matchedRules = _rules.value.filter { rule ->
            smsText.contains(rule.keyword, ignoreCase = true)
        }
        
        return if (matchedRules.isNotEmpty()) {
            val rule = matchedRules.first()
            "Matched: ${rule.extractedCategory} (${rule.keyword})"
        } else {
            // Try to detect amount
            val amountRegex = Regex("""(?:Rs\.?|INR)\s*([0-9,]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
            val amountMatch = amountRegex.find(smsText)
            
            if (amountMatch != null) {
                "Amount detected: ₹${amountMatch.groupValues[1]} (No category match)"
            } else {
                "No matching rules found"
            }
        }
    }
}

data class KeywordRulesUiState(
    val isLoading: Boolean = false
)
