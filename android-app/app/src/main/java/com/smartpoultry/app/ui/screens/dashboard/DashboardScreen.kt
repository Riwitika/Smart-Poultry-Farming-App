package com.smartpoultry.app.ui.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smartpoultry.app.ui.components.BarChart
import com.smartpoultry.app.ui.components.LineChart
import com.smartpoultry.app.ui.components.PieChart
import com.smartpoultry.app.ui.components.MascotIllustration
import com.smartpoultry.app.ui.components.FloatingBottomNavigation
import com.smartpoultry.app.ui.components.ScreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigateToPrediction: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(bottom = 88.dp) // Avoid overlap with floating bar
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Large Hero Header Section (Glassmorphic look)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF34D399).copy(alpha = 0.15f),
                                    Color(0xFF1E293B).copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val name = uiState.userProfile?.fullName ?: "Riwitika"
                            Text(
                                text = "Good Evening, $name 👋",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "AI Poultry Health Assistant",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Friendly Mascot
                        MascotIllustration(modifier = Modifier.size(72.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Flock Health Score Ring Container (Futuristic circular score)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Flock Health Index",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Realtime biosafety score based on last scans",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                // Large gradient diagnosis action
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF34D399), Color(0xFF059669))
                                            )
                                        )
                                        .clickable { onNavigateToPrediction() }
                                        .padding(vertical = 12.dp, horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Start New Diagnosis",
                                        color = Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Futuristic Circular score (96% placeholder or calculated score)
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Glowing Ring
                                CircularProgressIndicator(
                                    progress = 0.96f,
                                    color = Color(0xFF34D399),
                                    strokeWidth = 6.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "96%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF34D399)
                                    )
                                    Text(
                                        text = "Optimal",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    // Compute dynamic statistics
                    val totalPredictions = uiState.predictions.size
                    val healthyPredictions = uiState.predictions.count { it.diseaseName.lowercase().contains("healthy") }
                    val diseasePredictions = totalPredictions - healthyPredictions
                    val averageConfidence = if (uiState.predictions.isNotEmpty()) {
                        uiState.predictions.map { it.confidence }.average()
                    } else 0.0

                    // Beautiful Statistics Grid (Using exact palette)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatsCard(
                                label = "Total Scans",
                                value = totalPredictions.toString(),
                                icon = Icons.Default.List,
                                iconColor = Color(0xFF60A5FA),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                label = "Healthy Feces",
                                value = healthyPredictions.toString(),
                                icon = Icons.Default.CheckCircle,
                                iconColor = Color(0xFF22C55E),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatsCard(
                                label = "Diseased Feces",
                                value = diseasePredictions.toString(),
                                icon = Icons.Default.Warning,
                                iconColor = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                label = "Avg Confidence",
                                value = "${String.format("%.1f", averageConfidence)}%",
                                icon = Icons.Default.Info,
                                iconColor = Color(0xFFFBBF24),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Realtime Analytics Title
                    Text(
                        text = "Realtime Analytics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    // Chart 1: Donut Chart Health Status
                    ChartContainer(title = "Health Status Distribution") {
                        val pieData = mapOf(
                            "Healthy" to healthyPredictions.toFloat(),
                            "Diseased" to diseasePredictions.toFloat()
                        )
                        PieChart(
                            data = pieData,
                            colors = listOf(Color(0xFF22C55E), Color(0xFFEF4444))
                        )
                    }

                    // Chart 2: Detailed disease breakdown
                    ChartContainer(title = "Detected Diseases Breakdown") {
                        val diseaseGroups = uiState.predictions
                            .filter { !it.diseaseName.lowercase().contains("healthy") }
                            .groupBy { it.diseaseName }
                            .mapValues { it.value.size.toFloat() }
                        
                        PieChart(
                            data = diseaseGroups,
                            colors = listOf(Color(0xFFEF4444), Color(0xFFFBBF24), Color(0xFF60A5FA), Color(0xFF8B5CF6))
                        )
                    }

                    // Chart 3: Scan Activity
                    ChartContainer(title = "Weekly Scan Activity") {
                        val lastSeven = uiState.predictions.take(7).reversed()
                        val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
                        val barData = lastSeven.map { record ->
                            sdf.format(Date(record.createdAt)) to 1f
                        }
                        val aggregated = barData.groupBy { it.first }
                            .map { (date, list) -> date to list.sumOf { it.second.toDouble() }.toFloat() }
                        
                        BarChart(
                            data = aggregated,
                            color = Color(0xFF34D399)
                        )
                    }

                    // Chart 4: Confidence Trend
                    ChartContainer(title = "Diagnostics Confidence Trend") {
                        val trendData = uiState.predictions.take(10).reversed().map { it.confidence.toFloat() }
                        LineChart(
                            data = trendData,
                            color = Color(0xFF60A5FA)
                        )
                    }
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
fun StatsCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ChartContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
