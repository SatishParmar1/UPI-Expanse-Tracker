package com.hello.lets.test.screen.ui.onboarding

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.animation.ExperimentalAnimationApi

/**
 * Onboarding screen for first-time users.
 * Collects name, phone, and bank accounts.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val primaryGreen = Color(0xFF4CAF50)
    val accentGreen = Color(0xFF00E676)
    val darkBackground = Color(0xFF0D1F17)
    val surfaceColor = Color(0xFF1A3327)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Progress indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index <= uiState.currentStep) primaryGreen
                                else Color.White.copy(alpha = 0.2f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Content based on step
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() with
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "stepContent"
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(
                        onNext = { viewModel.nextStep() }
                    )
                    1 -> ProfileStep(
                        name = uiState.name,
                        phone = uiState.phone,
                        onNameChange = { viewModel.updateName(it) },
                        onPhoneChange = { viewModel.updatePhone(it) },
                        onNext = { viewModel.nextStep() },
                        onBack = { viewModel.previousStep() }
                    )
                    2 -> BankAccountsStep(
                        bankAccounts = uiState.bankAccounts,
                        onAddBank = { name, code, account ->
                            viewModel.addBankAccount(name, code, account)
                        },
                        onRemoveBank = { viewModel.removeBankAccount(it) },
                        onComplete = {
                            viewModel.completeOnboarding()
                            onComplete()
                        },
                        onBack = { viewModel.previousStep() }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val primaryGreen = Color(0xFF4CAF50)
    val accentGreen = Color(0xFF00E676)
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(primaryGreen, accentGreen)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to\nUPI Expense Tracker",
            fontSize = 28.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Track your transactions automatically from SMS.\nYour data stays on your device.",
            fontSize = 15.sp,
            fontFamily = LiterataFontFamily,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Features
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureItem(
                icon = Icons.Rounded.Lock,
                title = "100% Private",
                description = "Data never leaves your device"
            )
            FeatureItem(
                icon = Icons.Rounded.AutoAwesome,
                title = "Auto Categorization",
                description = "Smart transaction categorization"
            )
            FeatureItem(
                icon = Icons.Rounded.AccountBalance,
                title = "Multi-Bank Support",
                description = "Track all your bank accounts"
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    val primaryGreen = Color(0xFF4CAF50)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(primaryGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 13.sp,
                fontFamily = LiterataFontFamily,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ProfileStep(
    name: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = Color(0xFF1A3327)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Let's set up\nyour profile",
            fontSize = 28.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 36.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "This helps personalize your experience.",
            fontSize = 15.sp,
            fontFamily = LiterataFontFamily,
            color = Color.White.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Name field
        Text(
            text = "YOUR NAME",
            fontSize = 11.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Enter your name",
                    fontFamily = LiterataFontFamily,
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = primaryGreen
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = surfaceColor,
                focusedContainerColor = surfaceColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryGreen,
                cursorColor = primaryGreen,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontFamily = LiterataFontFamily,
                fontSize = 16.sp
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Phone field
        Text(
            text = "PHONE NUMBER (OPTIONAL)",
            fontSize = 11.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Enter phone number",
                    fontFamily = LiterataFontFamily,
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Phone,
                    contentDescription = null,
                    tint = primaryGreen
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = surfaceColor,
                focusedContainerColor = surfaceColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryGreen,
                cursorColor = primaryGreen,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = LiterataFontFamily,
                fontSize = 16.sp
            )
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotBlank()
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.ArrowForward, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankAccountsStep(
    bankAccounts: List<BankAccountEntry>,
    onAddBank: (String, String, String?) -> Unit,
    onRemoveBank: (Int) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val primaryGreen = Color(0xFF4CAF50)
    val surfaceColor = Color(0xFF1A3327)
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Common Indian banks
    val commonBanks = listOf(
        "HDFC Bank" to "HDFC",
        "State Bank of India" to "SBI",
        "ICICI Bank" to "ICICI",
        "Axis Bank" to "AXIS",
        "Kotak Mahindra Bank" to "KOTAK",
        "Yes Bank" to "YES",
        "Punjab National Bank" to "PNB",
        "Bank of Baroda" to "BOB",
        "IDFC First Bank" to "IDFC",
        "IndusInd Bank" to "INDUS",
        "Paytm Payments Bank" to "PAYTM"
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Add your\nbank accounts",
            fontSize = 28.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 36.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "We'll auto-detect transactions from these banks.",
            fontSize = 15.sp,
            fontFamily = LiterataFontFamily,
            color = Color.White.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Added banks
        if (bankAccounts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bankAccounts.size) { index ->
                    val bank = bankAccounts[index]
                    BankAccountCard(
                        bankName = bank.name,
                        bankCode = bank.code,
                        accountNumber = bank.accountNumber,
                        onRemove = { onRemoveBank(index) }
                    )
                }
                
                item {
                    // Add more button
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryGreen
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(primaryGreen, primaryGreen))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Another Bank", fontFamily = LiterataFontFamily)
                    }
                }
            }
        } else {
            // Empty state - show quick add buttons
            Text(
                text = "POPULAR BANKS",
                fontSize = 11.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonBanks) { (name, code) ->
                    BankQuickAddItem(
                        bankName = name,
                        bankCode = code,
                        onClick = { onAddBank(name, code, null) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip or Complete button
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (bankAccounts.isEmpty()) "Skip for Now" else "Complete Setup",
                fontSize = 16.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.Check, contentDescription = null)
        }
    }
    
    // Add bank dialog
    if (showAddDialog) {
        val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<OnboardingViewModel>()
        AddBankDialog(
            banks = commonBanks,
            onDismiss = { showAddDialog = false },
            onAdd = { name, code, account ->
                onAddBank(name, code, account)
                showAddDialog = false
            },
            onVerifyIfsc = { ifsc, callback ->
                viewModel.fetchIfscDetails(ifsc, callback)
            }
        )
    }
}

@Composable
private fun BankQuickAddItem(
    bankName: String,
    bankCode: String,
    onClick: () -> Unit
) {
    val bankColor = Color(android.graphics.Color.parseColor(BankCodes.getBankColor(bankCode)))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A3327))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bankColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bankCode.take(2),
                fontSize = 14.sp,
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = bankColor
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = bankName,
            fontSize = 15.sp,
            fontFamily = LiterataFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BankAccountCard(
    bankName: String,
    bankCode: String,
    accountNumber: String?,
    onRemove: () -> Unit
) {
    val bankColor = Color(android.graphics.Color.parseColor(BankCodes.getBankColor(bankCode)))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A3327))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bankColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bankCode.take(2),
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = bankColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bankName,
                    fontSize = 16.sp,
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (accountNumber != null) {
                    Text(
                        text = "••••$accountNumber",
                        fontSize = 13.sp,
                        fontFamily = LiterataFontFamily,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Remove",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBankDialog(
    banks: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String?) -> Unit,
    onVerifyIfsc: (String, (String?, String?, String?) -> Unit) -> Unit
) {
    var selectedBank by remember { mutableStateOf<Pair<String, String>?>(null) }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var ifscError by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A3327),
        title = {
            Text(
                "Add Bank Account",
                fontFamily = LiterataFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                if (selectedBank == null) {
                    // Method selection
                    Text(
                        "Search by IFSC",
                        fontFamily = LiterataFontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { 
                            if (it.length <= 11) {
                                ifscCode = it.uppercase()
                                ifscError = null
                                
                                if (it.length == 11) {
                                    isVerifying = true
                                    onVerifyIfsc(it) { name, code, error ->
                                        isVerifying = false
                                        if (name != null && code != null) {
                                            selectedBank = name to code
                                            android.widget.Toast.makeText(context, "Bank Verified: $name", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            ifscError = error
                                            android.widget.Toast.makeText(context, error ?: "Verification failed", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Enter IFSC Code",
                                fontFamily = LiterataFontFamily,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        },
                        trailingIcon = {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF4CAF50),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF0D1F17),
                            focusedContainerColor = Color(0xFF0D1F17),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedBorderColor = if (ifscError != null) Color.Red else Color(0xFF4CAF50),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        isError = ifscError != null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (ifscError != null) {
                        Text(
                            text = ifscError!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                        Text(
                            " OR ",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                        Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Bank selection dropdown
                Text(
                    "Select Bank",
                    fontFamily = LiterataFontFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedBank?.first ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = {
                            Text(
                                "Select bank",
                                fontFamily = LiterataFontFamily,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        },
                        trailingIcon = {
                            if (selectedBank != null) {
                                IconButton(onClick = { selectedBank = null; ifscCode = "" }) {
                                    Icon(Icons.Rounded.Close, null, tint = Color.White.copy(alpha = 0.6f))
                                }
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF0D1F17),
                            focusedContainerColor = Color(0xFF0D1F17),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1A3327))
                    ) {
                        banks.forEach { bank ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        bank.first,
                                        fontFamily = LiterataFontFamily,
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    selectedBank = bank
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { if (it.length <= 4) accountNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Last 4 digits (optional)",
                            fontFamily = LiterataFontFamily,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF0D1F17),
                        focusedContainerColor = Color(0xFF0D1F17),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedBorderColor = Color(0xFF4CAF50),
                        cursorColor = Color(0xFF4CAF50),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedBank?.let { (name, code) ->
                        onAdd(name, code, accountNumber.takeIf { it.isNotBlank() })
                    }
                },
                enabled = selectedBank != null
            ) {
                Text(
                    "Add",
                    color = Color(0xFF4CAF50),
                    fontFamily = LiterataFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = LiterataFontFamily
                )
            }
        }
    )
}

// Data classes
data class BankAccountEntry(
    val name: String,
    val code: String,
    val accountNumber: String? = null
)

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val phone: String = "",
    val bankAccounts: List<BankAccountEntry> = emptyList()
)

// ViewModel
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val userProfileDao = db.userProfileDao()
    private val bankAccountDao = db.bankAccountDao()
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    fun nextStep() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
    }
    
    fun previousStep() {
        _uiState.value = _uiState.value.copy(currentStep = maxOf(0, _uiState.value.currentStep - 1))
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }
    
    fun addBankAccount(name: String, code: String, accountNumber: String?) {
        val current = _uiState.value.bankAccounts.toMutableList()
        // Don't add duplicate
        if (current.none { it.code == code }) {
            current.add(BankAccountEntry(name, code, accountNumber))
            _uiState.value = _uiState.value.copy(bankAccounts = current)
        }
    }
    
    fun removeBankAccount(index: Int) {
        val current = _uiState.value.bankAccounts.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(bankAccounts = current)
        }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            // Save user profile
            val profile = UserProfile(
                name = _uiState.value.name.ifBlank { "User" },
                phoneNumber = _uiState.value.phone.takeIf { it.isNotBlank() },
                onboardingComplete = true
            )
            userProfileDao.insert(profile)
            
            // Save bank accounts
            _uiState.value.bankAccounts.forEachIndexed { index, entry ->
                // Register custom banks if they don't exist in default map
                BankCodes.addCustomBank(entry.code, entry.code, entry.name)
                
                val account = BankAccount(
                    bankName = entry.name,
                    bankCode = entry.code,
                    accountNumber = entry.accountNumber,
                    isDefault = index == 0,
                    colorHex = BankCodes.getBankColor(entry.code)
                )
                bankAccountDao.insert(account)
            }
        }
    }
    
    fun fetchIfscDetails(ifsc: String, onResult: (String?, String?, String?) -> Unit) {
        if (ifsc.length != 11) {
            onResult(null, null, "IFSC must be 11 characters")
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://ifsc.razorpay.com/$ifsc")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val bankName = json.optString("BANK", "Unknown Bank")
                    val bankCode = json.optString("BANKCODE", ifsc.take(4))
                    
                    withContext(Dispatchers.Main) {
                        onResult(bankName, bankCode, null)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(null, null, "Invalid IFSC Code or Bank not found")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null, null, e.message ?: "Network Error")
                }
            }
        }
    }
}
