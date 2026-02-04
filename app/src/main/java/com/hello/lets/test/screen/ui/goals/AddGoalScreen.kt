package com.hello.lets.test.screen.ui.goals

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Add Goal screen for creating new savings goals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    viewModel: AddGoalViewModel = viewModel(),
    onBackClick: () -> Unit,
    onGoalCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Handle successful save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onGoalCreated()
        }
    }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CREATE",
                            fontSize = 10.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "New Goal",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Goal Name
            InputField(
                label = "GOAL NAME",
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "e.g., Emergency Fund"
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Target Amount
            InputField(
                label = "TARGET AMOUNT",
                value = uiState.targetAmount,
                onValueChange = { viewModel.updateTargetAmount(it) },
                placeholder = "₹0.00",
                keyboardType = KeyboardType.Decimal,
                prefix = "₹"
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Description
            InputField(
                label = "DESCRIPTION (Optional)",
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                placeholder = "Add a description...",
                singleLine = false,
                minHeight = 80.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Priority Selection
            Text(
                text = "PRIORITY",
                fontSize = 10.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PriorityChip(
                    label = "High",
                    isSelected = uiState.priority == GoalPriority.HIGH,
                    color = Color(0xFFEF4444),
                    onClick = { viewModel.updatePriority(GoalPriority.HIGH) },
                    modifier = Modifier.weight(1f)
                )
                PriorityChip(
                    label = "Medium",
                    isSelected = uiState.priority == GoalPriority.MEDIUM,
                    color = Color(0xFFFF9800),
                    onClick = { viewModel.updatePriority(GoalPriority.MEDIUM) },
                    modifier = Modifier.weight(1f)
                )
                PriorityChip(
                    label = "Low",
                    isSelected = uiState.priority == GoalPriority.LOW,
                    color = Color(0xFF4CAF50),
                    onClick = { viewModel.updatePriority(GoalPriority.LOW) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Icon Selection
            Text(
                text = "ICON",
                fontSize = 10.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val icons = listOf(
                "shield" to Icons.Rounded.Shield,
                "flight" to Icons.Rounded.Flight,
                "laptop" to Icons.Rounded.LaptopMac,
                "home" to Icons.Rounded.Home,
                "car" to Icons.Rounded.DirectionsCar,
                "school" to Icons.Rounded.School,
                "health" to Icons.Rounded.LocalHospital,
                "savings" to Icons.Rounded.Savings
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.forEach { (iconName, icon) ->
                    IconOption(
                        icon = icon,
                        isSelected = uiState.iconName == iconName,
                        onClick = { viewModel.updateIcon(iconName) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Color Selection
            Text(
                text = "COLOR",
                fontSize = 10.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val colors = listOf(
                "#2196F3" to Color(0xFF2196F3),
                "#4CAF50" to Color(0xFF4CAF50),
                "#FF9800" to Color(0xFFFF9800),
                "#E91E63" to Color(0xFFE91E63),
                "#9C27B0" to Color(0xFF9C27B0),
                "#00BCD4" to Color(0xFF00BCD4)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { (hex, color) ->
                    ColorOption(
                        color = color,
                        isSelected = uiState.colorHex == hex,
                        onClick = { viewModel.updateColor(hex) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Smart Save Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Save",
                            fontSize = 16.sp,
                            fontFamily = LiterataFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Auto-save when you spend less",
                            fontSize = 12.sp,
                            fontFamily = LiterataFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.smartSaveEnabled,
                        onCheckedChange = { viewModel.toggleSmartSave() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryGreen
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Create Button
            Button(
                onClick = { viewModel.createGoal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState.isValid
            ) {
                Text(
                    text = "Create Goal",
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
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
                Text(
                    text = placeholder,
                    fontFamily = LiterataFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            prefix = if (prefix != null) {
                {
                    Text(
                        text = prefix,
                        fontFamily = LiterataFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = LiterataFontFamily,
                fontSize = 15.sp
            )
        )
    }
}

@Composable
private fun PriorityChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun IconOption(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryGreen = Color(0xFF4CAF50)
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) primaryGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) primaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) primaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ViewModel
class AddGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val savingsGoalDao = db.savingsGoalDao()
    
    private val _uiState = MutableStateFlow(AddGoalUiState())
    val uiState: StateFlow<AddGoalUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        validateForm()
    }
    
    fun updateTargetAmount(amount: String) {
        _uiState.value = _uiState.value.copy(targetAmount = amount)
        validateForm()
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun updatePriority(priority: GoalPriority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }
    
    fun updateIcon(iconName: String) {
        _uiState.value = _uiState.value.copy(iconName = iconName)
    }
    
    fun updateColor(colorHex: String) {
        _uiState.value = _uiState.value.copy(colorHex = colorHex)
    }
    
    fun toggleSmartSave() {
        _uiState.value = _uiState.value.copy(smartSaveEnabled = !_uiState.value.smartSaveEnabled)
    }
    
    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.name.isNotBlank() && 
                      state.targetAmount.isNotBlank() &&
                      state.targetAmount.toDoubleOrNull() != null &&
                      (state.targetAmount.toDoubleOrNull() ?: 0.0) > 0
        _uiState.value = state.copy(isValid = isValid)
    }
    
    fun createGoal() {
        val state = _uiState.value
        val targetAmount = state.targetAmount.toDoubleOrNull() ?: return
        
        viewModelScope.launch {
            val goal = SavingsGoal(
                name = state.name,
                description = state.description.ifBlank { null },
                targetAmount = targetAmount,
                savedAmount = 0.0,
                priority = state.priority,
                iconName = state.iconName,
                colorHex = state.colorHex,
                smartSaveEnabled = state.smartSaveEnabled,
                createdAt = System.currentTimeMillis()
            )
            savingsGoalDao.insert(goal)
            _uiState.value = state.copy(isSaved = true)
        }
    }
}

data class AddGoalUiState(
    val name: String = "",
    val targetAmount: String = "",
    val description: String = "",
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val iconName: String = "savings",
    val colorHex: String = "#4CAF50",
    val smartSaveEnabled: Boolean = false,
    val isValid: Boolean = false,
    val isSaved: Boolean = false
)
