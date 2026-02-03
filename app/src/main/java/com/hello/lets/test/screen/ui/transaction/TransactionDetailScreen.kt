package com.hello.lets.test.screen.ui.transaction

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.entity.Category
import com.hello.lets.test.data.entity.Transaction
import com.hello.lets.test.data.entity.TransactionType
import com.hello.lets.test.ui.theme.LiterataFontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Transaction details screen for viewing and editing transaction information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    viewModel: TransactionDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background
    
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TRANSACTION",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Details",
                            fontSize = 18.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        bottomBar = {
            // Save Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveTransaction()
                        onSaveClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Save Changes",
                        fontSize = 16.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Merchant Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.transaction?.merchant?.take(2)?.uppercase() ?: "TX",
                    fontSize = 24.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Category indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(primaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Amount
            val amount = uiState.transaction?.amount ?: 0.0
            val isDebit = uiState.transaction?.transactionType == TransactionType.DEBIT
            Text(
                text = "₹${String.format("%,.2f", amount)}",
                fontSize = 40.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isDebit) MaterialTheme.colorScheme.onSurface else primaryGreen
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Merchant and Date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = uiState.transaction?.transactionDate?.let { dateFormat.format(Date(it)) } ?: ""
            Text(
                text = "${uiState.transaction?.merchant ?: "Unknown"}  •  $dateStr",
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Processed Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROCESSED LOCALLY",
                        fontSize = 10.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Raw SMS Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Sms,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RAW SMS CONTENT",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = uiState.transaction?.rawSmsContent ?: "No SMS content available",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Editable Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Merchant Field
                EditableField(
                    label = "MERCHANT",
                    value = uiState.editedMerchant,
                    onValueChange = { viewModel.updateMerchant(it) },
                    showEditIcon = true
                )
                
                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                Column {
                    Text(
                        text = "CATEGORY",
                        fontSize = 11.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clip(RoundedCornerShape(12.dp))
                                .background(surfaceColor)
                                .clickable { categoryExpanded = true }
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
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF9800))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = uiState.selectedCategory?.name ?: "Select Category",
                                        fontSize = 15.sp,
                                        fontFamily = LiterataFontFamily,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.updateCategory(category)
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Date and Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATE",
                            fontSize = 11.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(surfaceColor)
                                .padding(16.dp)
                        ) {
                            val dateOnly = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            Text(
                                text = uiState.transaction?.transactionDate?.let { dateOnly.format(Date(it)) } ?: "",
                                fontSize = 15.sp,
                                fontFamily = LiterataFontFamily,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIME",
                            fontSize = 11.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(surfaceColor)
                                .padding(16.dp)
                        ) {
                            val timeOnly = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            Text(
                                text = uiState.transaction?.transactionDate?.let { timeOnly.format(Date(it)) } ?: "",
                                fontSize = 15.sp,
                                fontFamily = LiterataFontFamily,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // Notes Field
                EditableField(
                    label = "NOTES",
                    value = uiState.editedNotes,
                    onValueChange = { viewModel.updateNotes(it) },
                    placeholder = "Add a note...",
                    showEditIcon = false
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Technical Metadata
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "T E C H N I C A L   M E T A D A T A",
                    fontSize = 11.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                MetadataRow("Reference ID", uiState.transaction?.referenceId ?: "N/A")
                MetadataRow("Source Number", uiState.transaction?.smsAddress ?: "N/A")
                MetadataRow("Account", uiState.transaction?.accountNumber ?: "N/A")
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    showEditIcon: Boolean = false
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 15.sp,
                                fontFamily = LiterataFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )
                if (showEditIcon) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = modifier,
        decorationBox = decorationBox
    )
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ViewModel
class TransactionDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    
    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    init {
        viewModelScope.launch {
            db.categoryDao().getAllCategories().collect { cats ->
                _categories.value = cats
            }
        }
    }
    
    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = db.transactionDao().getById(id)
            transaction?.let { tx ->
                val category = tx.categoryId?.let { db.categoryDao().getById(it) }
                _uiState.value = TransactionDetailUiState(
                    transaction = tx,
                    editedMerchant = tx.merchant,
                    editedNotes = tx.notes ?: "",
                    selectedCategory = category
                )
            }
        }
    }
    
    fun updateMerchant(merchant: String) {
        _uiState.value = _uiState.value.copy(editedMerchant = merchant)
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(editedNotes = notes)
    }
    
    fun updateCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun saveTransaction() {
        viewModelScope.launch {
            _uiState.value.transaction?.let { tx ->
                val updated = tx.copy(
                    merchant = _uiState.value.editedMerchant,
                    notes = _uiState.value.editedNotes.ifEmpty { null },
                    categoryId = _uiState.value.selectedCategory?.id
                )
                db.transactionDao().update(updated)
            }
        }
    }
}

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val editedMerchant: String = "",
    val editedNotes: String = "",
    val selectedCategory: Category? = null
)
