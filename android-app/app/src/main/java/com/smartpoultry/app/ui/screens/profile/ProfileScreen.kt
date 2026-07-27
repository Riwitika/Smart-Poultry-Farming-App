package com.smartpoultry.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.smartpoultry.app.ui.components.FarmerIllustration
import com.smartpoultry.app.ui.components.FloatingBottomNavigation
import com.smartpoultry.app.ui.components.ScreenContainer
import com.smartpoultry.app.ui.screens.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF34D399))
            }
        } else {
            val profile = uiState.userProfile
            
            // Resolve display name and email fallback
            val resolvedName = profile?.fullName?.ifBlank { null }
                ?: firebaseUser?.displayName?.ifBlank { null }
                ?: "Farmer"
            val resolvedEmail = profile?.email?.ifBlank { null }
                ?: firebaseUser?.email?.ifBlank { null }
                ?: "farmer@poultry.com"

            val initials = resolvedName.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()

            val joinedDateText = if (profile != null) {
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(profile.createdAt))
            } else if (firebaseUser != null) {
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis()))
            } else {
                "Unknown Date"
            }

            val lastScanText = if (uiState.predictions.isNotEmpty()) {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(uiState.predictions.first().createdAt))
            } else {
                "No scans completed yet"
            }

            // Calculate stats dynamically from predictions list
            val totalScans = uiState.predictions.size
            val healthyScans = uiState.predictions.count { it.diseaseName.lowercase().contains("healthy") }
            val diseaseScans = totalScans - healthyScans

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(bottom = 88.dp) // Avoid bottom bar
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Hero profile backdrop
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF60A5FA).copy(alpha = 0.12f),
                                    Color(0xFF1E293B).copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Farmer Profile",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Diagnostics account credentials",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Farmer Illustration
                        FarmerIllustration(modifier = Modifier.size(72.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // User initials avatar
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF34D399), Color(0xFF60A5FA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Profile info card (Glassmorphic border details)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileInfoRow(
                                icon = Icons.Default.Person,
                                label = "Farmer Name",
                                value = resolvedName
                            )
                            ProfileInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = resolvedEmail
                            )
                            ProfileInfoRow(
                                icon = Icons.Default.CheckCircle,
                                label = "Joined Platform",
                                value = joinedDateText
                            )
                        }
                    }

                    // Achievement Badges Row (Futuristic achievement representation)
                    Text(
                        text = "Achievements & Badges",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BadgeItem(
                            label = "100 Scans",
                            iconColor = Color(0xFF34D399),
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            label = "Top Accuracy",
                            iconColor = Color(0xFF60A5FA),
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            label = "Active Farmer",
                            iconColor = Color(0xFFFBBF24),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Diagnostic statistics card
                    Text(
                        text = "Diagnostics Metrics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileStatRow(
                                icon = Icons.Default.List,
                                iconColor = Color(0xFF60A5FA),
                                label = "Total Scans Saved",
                                value = totalScans.toString()
                            )
                            ProfileStatRow(
                                icon = Icons.Default.CheckCircle,
                                iconColor = Color(0xFF22C55E),
                                label = "Healthy Fecal Samples",
                                value = healthyScans.toString()
                            )
                            ProfileStatRow(
                                icon = Icons.Default.Warning,
                                iconColor = Color(0xFFEF4444),
                                label = "Diseased Fecal Samples",
                                value = diseaseScans.toString()
                            )
                            ProfileStatRow(
                                icon = Icons.Default.Person,
                                iconColor = Color(0xFFFBBF24),
                                label = "Last Scan Date",
                                value = lastScanText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sign Out Button
                    OutlinedButton(
                        onClick = {
                            authViewModel.logout(onSuccess = onNavigateToLogin)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.5.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Overlay floating bottom bar
        FloatingBottomNavigation(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BadgeItem(
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(width = 1.dp, color = Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF60A5FA),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProfileStatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
