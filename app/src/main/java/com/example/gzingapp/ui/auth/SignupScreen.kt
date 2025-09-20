package com.example.gzingapp.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gzingapp.R

// Custom color scheme based on your palette
private val BeigeBrown = Color(0xFFF5E6D3)
private val BeigeBrownVariant = Color(0xFFE8D5C4)
private val PrimaryBrown = Color(0xFF8B4513)
private val PrimaryBrownVariant = Color(0xFFA0522D)
private val SecondaryCream = Color(0xFFFFF8DC)
private val SecondaryTan = Color(0xFFD2B48C)
private val SecondaryDarkBrown = Color(0xFF654321)
private val SecondaryLightBrown = Color(0xFFCD853F)
private val AccentGold = Color(0xFFDAA520)
private val AccentAmber = Color(0xFFFFBF00)
private val AccentRust = Color(0xFFB7410E)
private val SuccessGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFF44336)
private val GrayLight = Color(0xFFF5F5F5)
private val GrayMedium = Color(0xFFCCCCCC)
private val GrayDark = Color(0xFF666666)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupClick: (firstName: String, lastName: String, email: String, password: String, confirmPassword: String, phoneNumber: String, username: String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    val allFieldsValid = firstName.isNotBlank() && lastName.isNotBlank() && 
                        email.isNotBlank() && password.isNotBlank() && 
                        confirmPassword.isNotBlank() && phoneNumber.isNotBlank() && 
                        username.isNotBlank()
    
    val passwordsMatch = password == confirmPassword && password.isNotBlank()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SecondaryCream,
                        BeigeBrown.copy(alpha = 0.4f),
                        SecondaryTan.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Enhanced App Logo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AccentGold.copy(alpha = 0.2f),
                                    BeigeBrown.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(70.dp)
                    )
                }
                
                // Enhanced Welcome Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBrown,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                    
                    Text(
                        text = "Join us to get started on your journey",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GrayDark,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Main Signup Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = PrimaryBrown.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Error Message with enhanced styling
                    if (error != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorRed.copy(alpha = 0.1f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                ErrorRed.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = "Error",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = error,
                                    color = ErrorRed,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Personal Information Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBrown
                        )
                        
                        // Name Fields Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // First Name Field
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { 
                                    Text("First Name", color = GrayDark, fontWeight = FontWeight.Medium) 
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BeigeBrown.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person, 
                                            contentDescription = "First Name",
                                            tint = PrimaryBrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBrown,
                                    unfocusedBorderColor = GrayMedium,
                                    focusedLabelColor = PrimaryBrown,
                                    cursorColor = PrimaryBrown
                                )
                            )
                            
                            // Last Name Field
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { 
                                    Text("Last Name", color = GrayDark, fontWeight = FontWeight.Medium) 
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BeigeBrown.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person, 
                                            contentDescription = "Last Name",
                                            tint = PrimaryBrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBrown,
                                    unfocusedBorderColor = GrayMedium,
                                    focusedLabelColor = PrimaryBrown,
                                    cursorColor = PrimaryBrown
                                )
                            )
                        }
                    }
                    
                    // Account Information Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Account Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBrown
                        )
                        
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { 
                                Text("Email Address", color = GrayDark, fontWeight = FontWeight.Medium) 
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BeigeBrown.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Email, 
                                        contentDescription = "Email",
                                        tint = PrimaryBrown,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBrown,
                                unfocusedBorderColor = GrayMedium,
                                focusedLabelColor = PrimaryBrown,
                                cursorColor = PrimaryBrown
                            ),
                            placeholder = { Text("Enter your email", color = GrayMedium) }
                        )
                        
                        // Username Field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { 
                                Text("Username", color = GrayDark, fontWeight = FontWeight.Medium) 
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BeigeBrown.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AlternateEmail, 
                                        contentDescription = "Username",
                                        tint = PrimaryBrown,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBrown,
                                unfocusedBorderColor = GrayMedium,
                                focusedLabelColor = PrimaryBrown,
                                cursorColor = PrimaryBrown
                            ),
                            placeholder = { Text("Choose a username", color = GrayMedium) }
                        )
                        
                        // Phone Number Field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { 
                                Text("Phone Number", color = GrayDark, fontWeight = FontWeight.Medium) 
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BeigeBrown.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Phone, 
                                        contentDescription = "Phone Number",
                                        tint = PrimaryBrown,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBrown,
                                unfocusedBorderColor = GrayMedium,
                                focusedLabelColor = PrimaryBrown,
                                cursorColor = PrimaryBrown
                            ),
                            placeholder = { Text("Enter your phone number", color = GrayMedium) }
                        )
                    }
                    
                    // Security Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Security",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBrown
                        )
                        
                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { 
                                Text("Password", color = GrayDark, fontWeight = FontWeight.Medium) 
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BeigeBrown.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lock, 
                                        contentDescription = "Password",
                                        tint = PrimaryBrown,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) 
                                            Icons.Default.Visibility 
                                        else 
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) 
                                            "Hide password" 
                                        else 
                                            "Show password",
                                        tint = GrayDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBrown,
                                unfocusedBorderColor = GrayMedium,
                                focusedLabelColor = PrimaryBrown,
                                cursorColor = PrimaryBrown
                            ),
                            placeholder = { Text("Create a strong password", color = GrayMedium) }
                        )
                        
                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { 
                                Text("Confirm Password", color = GrayDark, fontWeight = FontWeight.Medium) 
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (passwordsMatch && confirmPassword.isNotBlank()) 
                                                SuccessGreen.copy(alpha = 0.2f) 
                                            else if (confirmPassword.isNotBlank() && !passwordsMatch) 
                                                ErrorRed.copy(alpha = 0.2f)
                                            else 
                                                BeigeBrown.copy(alpha = 0.3f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lock, 
                                        contentDescription = "Confirm Password",
                                        tint = if (passwordsMatch && confirmPassword.isNotBlank()) 
                                            SuccessGreen
                                        else if (confirmPassword.isNotBlank() && !passwordsMatch) 
                                            ErrorRed
                                        else 
                                            PrimaryBrown,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) 
                                            Icons.Default.Visibility 
                                        else 
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) 
                                            "Hide password" 
                                        else 
                                            "Show password",
                                        tint = GrayDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (passwordsMatch && confirmPassword.isNotBlank()) 
                                    SuccessGreen
                                else if (confirmPassword.isNotBlank() && !passwordsMatch) 
                                    ErrorRed
                                else 
                                    PrimaryBrown,
                                unfocusedBorderColor = if (passwordsMatch && confirmPassword.isNotBlank()) 
                                    SuccessGreen.copy(alpha = 0.7f)
                                else if (confirmPassword.isNotBlank() && !passwordsMatch) 
                                    ErrorRed.copy(alpha = 0.7f)
                                else 
                                    GrayMedium,
                                focusedLabelColor = if (passwordsMatch && confirmPassword.isNotBlank()) 
                                    SuccessGreen
                                else if (confirmPassword.isNotBlank() && !passwordsMatch) 
                                    ErrorRed
                                else 
                                    PrimaryBrown,
                                cursorColor = PrimaryBrown
                            ),
                            placeholder = { Text("Confirm your password", color = GrayMedium) },
                            isError = confirmPassword.isNotBlank() && !passwordsMatch
                        )
                        
                        // Password match indicator
                        if (confirmPassword.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (passwordsMatch) SuccessGreen else ErrorRed
                                        )
                                )
                                Text(
                                    text = if (passwordsMatch) "Passwords match" else "Passwords don't match",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (passwordsMatch) SuccessGreen else ErrorRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Signup Button
                    Button(
                        onClick = { 
                            onSignupClick(firstName, lastName, email, password, confirmPassword, phoneNumber, username)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        enabled = !isLoading && allFieldsValid && passwordsMatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBrown,
                            contentColor = Color.White,
                            disabledContainerColor = GrayMedium
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Creating Account...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Create Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Login Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BeigeBrown.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GrayDark,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    
                    OutlinedButton(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBrown,
                            containerColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp, 
                            PrimaryBrown
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Sign In Instead",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
        
        // Decorative elements
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset((-30).dp, 80.dp)
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = 0.1f))
        )
        
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(340.dp, 200.dp)
                .clip(CircleShape)
                .background(SecondaryTan.copy(alpha = 0.15f))
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(20.dp, 800.dp)
                .clip(CircleShape)
                .background(PrimaryBrown.copy(alpha = 0.1f))
        )
    }
}






