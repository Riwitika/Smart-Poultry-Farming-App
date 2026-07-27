package com.smartpoultry.app.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.smartpoultry.app.domain.model.PredictionRecord
import com.smartpoultry.app.ui.components.ClipboardIllustration
import com.smartpoultry.app.ui.components.FloatingBottomNavigation
import com.smartpoultry.app.ui.components.ScreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(bottom = 88.dp) // Leave space for bottom bar
        ) {
            // Header (Futuristic look)
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
                Column {
                    Text(
                        text = "Diagnostics History",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Realtime historical reports log",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search diagnostics...", color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF34D399),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    singleLine = true
                )

                // Filter Chips Row
                val filterCategories = listOf("All", "Healthy", "Coccidiosis", "Salmonella", "Newcastle Disease")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    items(filterCategories) { category ->
                        val isSelected = uiState.filterDisease == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilterDisease(category) },
                            label = { Text(category) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF34D399).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF34D399),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = isSelected,
                                enabled = true,
                                borderColor = Color.White.copy(alpha = 0.08f),
                                selectedBorderColor = Color(0xFF34D399),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp
                            )
                        )
                    }
                }

                // Predictions List View
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF34D399))
                    }
                } else if (uiState.predictions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ClipboardIllustration(modifier = Modifier.size(96.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Reports Logged",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Run sample scans on the dashboard to log records.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.predictions, key = { it.predictionId }) { record ->
                            HistoryRecordCard(
                                record = record,
                                onDelete = { viewModel.deletePrediction(record.predictionId) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Overlay floating bottom bar
        FloatingBottomNavigation(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
        )
    }
}

@Composable
fun HistoryRecordCard(
    record: PredictionRecord,
    onDelete: () -> Unit
) {
    val isHealthy = record.diseaseName.lowercase().contains("healthy")
    val accentColor = if (isHealthy) Color(0xFF22C55E) else Color(0xFFEF4444)

    val sdfDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateStr = sdfDate.format(Date(record.createdAt))
    val timeStr = sdfTime.format(Date(record.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Crop Image preview
            AsyncImage(
                model = record.imageUrl,
                contentDescription = "Fecal Sample Thumbnail",
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: Diagnostic details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.diseaseName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )

                // Confidence Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Confidence: ${record.confidence}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Date Time display
                Text(
                    text = "$dateStr • $timeStr",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Right: Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Color(0xFFEF4444).copy(alpha = 0.08f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Record",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
