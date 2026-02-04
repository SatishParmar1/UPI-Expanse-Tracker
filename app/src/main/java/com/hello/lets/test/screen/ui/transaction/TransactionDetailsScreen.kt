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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Transaction Details screen showing full transaction information with edit capability.
 * Based on the user's mockup design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    transactionId: Long,
    viewModel: TransactionDetailsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Load transaction when screen opens
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TRANSACTION",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Details",
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
                    IconButton(onClick = { viewModel.showDeleteConfirmation() }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryGreen)
            }
        } else if (uiState.transaction == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Transaction not found",
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Merchant Logo/Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.editedMerchant.take(2).uppercase(),
                        fontSize = 24.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Amount
                val transaction = uiState.transaction!!
                val isCredit = transaction.transactionType == TransactionType.CREDIT
                val amountColor = if (isCredit) primaryGreen else MaterialTheme.colorScheme.onSurface
                
                Text(
                    text = "₹${String.format(Locale.US, "%,.2f", transaction.amount)}",
                    fontSize = 36.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Merchant and Date
                Text(
                    text = "${uiState.editedMerchant} · ${formatDateTime(transaction.transactionDate)}",
                    fontSize = 14.sp,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Processed Locally Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(primaryGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROCESSED LOCALLY",
                        fontSize = 10.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Raw SMS Content Card
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
                                fontSize = 11.sp,
                                fontFamily = LiterataFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = transaction.rawSmsContent,
                            fontSize = 13.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Editable Fields
                
                // Merchant Field
                EditableField(
                    label = "MERCHANT",
                    value = uiState.editedMerchant,
                    onValueChange = { viewModel.updateMerchant(it) },
                    icon = Icons.Rounded.Edit
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category Dropdown
                CategoryDropdown(
                    label = "CATEGORY",
                    selectedCategoryId = uiState.editedCategoryId,
                    categories = categories,
                    onCategorySelected = { viewModel.updateCategory(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date and Time (Read-only)
                Row(modifier = Modifier.fillMaxWidth()) {
                    ReadOnlyField(
                        label = "DATE",
                        value = formatDate(transaction.transactionDate),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    ReadOnlyField(
                        label = "TIME",
                        value = formatTime(transaction.transactionDate),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes Field (Editable)
                EditableField(
                    label = "NOTES / DESCRIPTION",
                    value = uiState.editedNotes,
                    onValueChange = { viewModel.updateNotes(it) },
                    placeholder = "Add a note...",
                    singleLine = false,
                    minHeight = 80.dp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Technical Metadata Section
                Text(
                    text = "TECHNICAL METADATA",
                    fontSize = 11.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                MetadataRow("Reference ID", transaction.referenceId ?: "N/A")
                MetadataRow("Source Number", transaction.smsAddress)
                MetadataRow("Account", transaction.accountNumber?.let { "XXXX...$it" } ?: "N/A")
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Save Button
                Button(
                    onClick = {
                        viewModel.saveChanges()
                        onSaveSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState.hasChanges
                ) {
                    Text(
                        text = "Save Changes",
                        fontSize = 16.sp,
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = {
                Text(
                    text = "Delete Transaction",
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this transaction? This action cannot be undone.",
                    fontFamily = LiterataFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction()
                        onBackClick()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFEF4444),
                        fontFamily = LiterataFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancel", fontFamily = LiterataFontFamily)
                }
            }
        )
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            },
            trailingIcon = if (icon != null) {
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = surfaceColor,
                focusedContainerColor = surfaceColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color(0xFF4CAF50)
            ),
            singleLine = singleLine,
            textStyle = LocalTextStyle.current.copy(
                fontFamily = LiterataFontFamily,
                fontSize = 15.sp
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    label: String,
    selectedCategoryId: Long?,
    categories: List<Category>,
    onCategorySelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryGreen = Color(0xFF4CAF50)
    
    val selectedCategory = categories.find { it.id == selectedCategoryId }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Uncategorized",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedCategory != null) {
                                    try {
                                        Color(android.graphics.Color.parseColor(selectedCategory.colorHex))
                                    } catch (e: Exception) {
                                        primaryGreen
                                    }
                                } else primaryGreen
                            )
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = surfaceColor,
                    focusedContainerColor = surfaceColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = primaryGreen
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = LiterataFontFamily,
                    fontSize = 15.sp
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Uncategorized", fontFamily = LiterataFontFamily)
                        }
                    },
                    onClick = {
                        onCategorySelected(null)
                        expanded = false
                    }
                )
                categories.forEach { category ->
                    val categoryColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        primaryGreen
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(categoryColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(category.name, fontFamily = LiterataFontFamily)
                            }
                        },
                        onClick = {
                            onCategorySelected(category.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceColor)
                .padding(16.dp)
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontFamily = LiterataFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
            fontFamily = LiterataFontFamily,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper functions
private fun formatDateTime(timestamp: Long): String {
    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

// ViewModel
class TransactionDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    
    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()
    
    val categories: StateFlow<List<Category>> = categoryDao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val transaction = transactionDao.getById(id)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                transaction = transaction,
                editedMerchant = transaction?.merchant ?: "",
                editedCategoryId = transaction?.categoryId,
                editedNotes = transaction?.notes ?: ""
            )
        }
    }
    
    fun updateMerchant(merchant: String) {
        _uiState.value = _uiState.value.copy(
            editedMerchant = merchant,
            hasChanges = true
        )
    }
    
    fun updateCategory(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(
            editedCategoryId = categoryId,
            hasChanges = true
        )
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(
            editedNotes = notes,
            hasChanges = true
        )
    }
    
    fun saveChanges() {
        val transaction = _uiState.value.transaction ?: return
        viewModelScope.launch {
            val updatedTransaction = transaction.copy(
                merchant = _uiState.value.editedMerchant,
                categoryId = _uiState.value.editedCategoryId,
                notes = _uiState.value.editedNotes
            )
            transactionDao.update(updatedTransaction)
            _uiState.value = _uiState.value.copy(
                transaction = updatedTransaction,
                hasChanges = false
            )
        }
    }
    
    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }
    
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }
    
    fun deleteTransaction() {
        val transaction = _uiState.value.transaction ?: return
        viewModelScope.launch {
            transactionDao.delete(transaction)
        }
    }
}

data class TransactionDetailsUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    val editedMerchant: String = "",
    val editedCategoryId: Long? = null,
    val editedNotes: String = "",
    val hasChanges: Boolean = false,
    val showDeleteDialog: Boolean = false
)
