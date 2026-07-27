package com.smartpoultry.app.ui.screens.dashboard

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.smartpoultry.app.domain.model.PredictionRecord
import com.smartpoultry.app.ui.components.BarChart
import com.smartpoultry.app.ui.components.LineChart
import com.smartpoultry.app.ui.components.PieChart
import com.smartpoultry.app.ui.components.ScreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToPrediction: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenContainer(
        title = "Dashboard",
        onBackClick = null
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Welcome Header
                val name = uiState.userProfile?.fullName ?: "Poultry Farmer"
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hello, $name",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Flock health diagnostic metrics & realtime stats",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Compute Stats
                val totalPredictions = uiState.predictions.size
                val healthyPredictions = uiState.predictions.count { it.diseaseName.lowercase().contains("healthy") }
                val diseasePredictions = totalPredictions - healthyPredictions
                val averageConfidence = if (uiState.predictions.isNotEmpty()) {
                    uiState.predictions.map { it.confidence }.average()
                } else 0.0
                
                val lastScanText = if (uiState.predictions.isNotEmpty()) {
                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    sdf.format(Date(uiState.predictions.first().createdAt))
                } else {
                    "No scans completed"
                }

                // Quick Statistics Grid (2x2 layout)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsCard(
                            label = "Total Scans",
                            value = totalPredictions.toString(),
                            icon = Icons.Default.List,
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatsCard(
                            label = "Healthy Feces",
                            value = healthyPredictions.toString(),
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF10B981),
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
                            iconColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Last scan summary row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Last Scan Completed:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lastScanText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // AI Diagnostics actions card
                DashboardCard(
                    title = "Disease Detection",
                    subtitle = "Run AI-powered diagnostics on fecal samples",
                    icon = Icons.Default.Favorite,
                    onClick = onNavigateToPrediction
                )

                // Prediction History Quick Link
                DashboardCard(
                    title = "Prediction History",
                    subtitle = "View diagnostic reports and history log",
                    icon = Icons.Default.List,
                    onClick = onNavigateToHistory
                )

                // CHARTS & ANALYTICS SECTION
                Text(
                    text = "Realtime Analytics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Chart 1: Healthy vs Diseased Pie Chart
                ChartContainer(title = "Health Status Distribution") {
                    val pieData = mapOf(
                        "Healthy" to healthyPredictions.toFloat(),
                        "Diseased" to diseasePredictions.toFloat()
                    )
                    PieChart(
                        data = pieData,
                        colors = listOf(Color(0xFF10B981), Color(0xFFEF4444))
                    )
                }

                // Chart 2: Disease Distribution detailed Pie Chart
                ChartContainer(title = "Detected Diseases Breakdown") {
                    // Group predictions that are not healthy by disease name
                    val diseaseGroups = uiState.predictions
                        .filter { !it.diseaseName.lowercase().contains("healthy") }
                        .groupBy { it.diseaseName }
                        .mapValues { it.value.size.toFloat() }
                    
                    PieChart(
                        data = diseaseGroups,
                        colors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF3B82F6), Color(0xFF8B5CF6))
                    )
                }

                // Chart 3: Predictions per Day Bar Chart (Last 7 days)
                ChartContainer(title = "Scan Activity (Last 7 Scans)") {
                    // Get last 7 scans chronologically
                    val lastSeven = uiState.predictions.take(7).reversed()
                    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val barData = lastSeven.map { record ->
                        sdf.format(Date(record.createdAt)) to 1f // show activity count
                    }
                    // Aggregate duplicates if they exist on the same day
                    val aggregated = barData.groupBy { it.first }
                        .map { (date, list) -> date to list.sumOf { it.second.toDouble() }.toFloat() }
                    
                    BarChart(
                        data = aggregated,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Chart 4: Confidence Trend Line Chart
                ChartContainer(title = "Diagnostics Confidence Trend (Last 10 Scans)") {
                    // Take last 10 confidence values chronologically (newest at right)
                    val trendData = uiState.predictions.take(10).reversed().map { it.confidence.toFloat() }
                    LineChart(
                        data = trendData,
                        color = Color(0xFF10B981)
                    )
                }

                // NAVIGATION QUICK ACTIONS SECTION
                Text(
                    text = "Account & Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCardSmall(
                        title = "Farmer Profile",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToProfile,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCardSmall(
                        title = "App Settings",
                        icon = Icons.Default.Settings,
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun DashboardCardSmall(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
