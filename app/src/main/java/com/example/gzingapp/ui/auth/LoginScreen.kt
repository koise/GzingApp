package com.example.gzingapp.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
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
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onSignupClick: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = PrimaryBrown.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // App Logo with enhanced styling
                    Box(
                        modifier = Modifier
                            .size(100.dp)
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
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    // Welcome Text with enhanced typography
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Welcome Back!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBrown,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Sign in to continue your journey",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GrayDark,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }

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
                                    Icons.Default.Lock,
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

                    // Input Fields Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Email Field with enhanced styling
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = {
                                Text(
                                    "Email Address",
                                    color = GrayDark,
                                    fontWeight = FontWeight.Medium
                                )
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
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBrown,
                                unfocusedBorderColor = GrayMedium,
                                focusedLabelColor = PrimaryBrown,
                                cursorColor = PrimaryBrown,
                                focusedLeadingIconColor = PrimaryBrown,
                                unfocusedLeadingIconColor = GrayDark
                            ),
                            placeholder = {
                                Text(
                                    "Enter your email",
                                    color = GrayMedium
                                )
                            }
                        )

                        // Password Field with enhanced styling
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = {
                                Text(
                                    "Password",
                                    color = GrayDark,
                                    fontWeight = FontWeight.Medium
                                )
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
                                        modifier = Modifier.size(20.dp)
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
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBrown,
                                unfocusedBorderColor = GrayMedium,
                                focusedLabelColor = PrimaryBrown,
                                cursorColor = PrimaryBrown,
                                focusedLeadingIconColor = PrimaryBrown,
                                unfocusedLeadingIconColor = GrayDark
                            ),
                            placeholder = {
                                Text(
                                    "Enter your password",
                                    color = GrayMedium
                                )
                            }
                        )
                    }

                    // Login Button with enhanced styling
                    Button(
                        onClick = { onLoginClick(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
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
                                    text = "Signing In...",
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
                                    Icons.Default.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Sign In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Section with enhanced styling
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
                        text = "New to our platform?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GrayDark,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    OutlinedButton(
                        onClick = onSignupClick,
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
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Create Account",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        }

        // Decorative elements
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset((-40).dp, (-40).dp)
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = 0.1f))
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(320.dp, 120.dp)
                .clip(CircleShape)
                .background(SecondaryTan.copy(alpha = 0.15f))
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(30.dp, 600.dp)
                .clip(CircleShape)
                .background(PrimaryBrown.copy(alpha = 0.1f))
        )
    }
}