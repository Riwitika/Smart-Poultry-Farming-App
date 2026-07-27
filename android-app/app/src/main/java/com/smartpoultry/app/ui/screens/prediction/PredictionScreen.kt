package com.smartpoultry.app.ui.screens.prediction

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.smartpoultry.app.ui.components.FloatingBottomNavigation
import com.smartpoultry.app.ui.components.MicroscopeIllustration
import com.smartpoultry.app.ui.components.PrimaryButton
import com.smartpoultry.app.ui.components.ScreenContainer
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PredictionScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isCameraMode by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                viewModel.resetState()
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isCameraMode = true
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Camera permission is required to capture photo")
                }
            }
        }
    )

    val firestoreError by viewModel.firestoreError.collectAsState()

    LaunchedEffect(firestoreError) {
        firestoreError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFirestoreError()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PredictionUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    if (isCameraMode) {
        CameraPreviewLayout(
            imageCapture = imageCapture,
            onImageCaptured = { uri ->
                selectedImageUri = uri
                isCameraMode = false
                viewModel.uploadImage(uri)
            },
            onClose = { isCameraMode = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(bottom = 88.dp) // Space for bottom bar
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
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
                            text = "AI Diagnostics Lab",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Futuristic YOLOv5 Fecal Diagnostics scan",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Upload Card with vector illustration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1E293B))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Fecal Sample Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                MicroscopeIllustration(modifier = Modifier.size(96.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ready to Scan",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Position camera or select fecal dropping image",
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                if (hasPermission) {
                                    isCameraMode = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF34D399)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF34D399)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }

                    // Analyze sample button (Gradient primary look)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selectedImageUri != null && uiState !is PredictionUiState.Loading) {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF34D399), Color(0xFF059669))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF1E293B), Color(0xFF1E293B))
                                    )
                                }
                            )
                            .clickable(enabled = selectedImageUri != null && uiState !is PredictionUiState.Loading) {
                                selectedImageUri?.let { viewModel.uploadImage(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState is PredictionUiState.Loading) {
                            CircularProgressIndicator(color = Color(0xFF0F172A), modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Analyze Sample",
                                color = if (selectedImageUri != null) Color(0xFF0F172A) else Color(0xFF64748B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Reveal Prediction Result Card
                    AnimatedVisibility(
                        visible = uiState is PredictionUiState.Success,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(500)
                        )
                    ) {
                        if (uiState is PredictionUiState.Success) {
                            val prediction = (uiState as PredictionUiState.Success).prediction
                            PremiumResultCard(prediction = prediction)
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
}

@Composable
fun PremiumResultCard(
    prediction: com.smartpoultry.app.domain.model.Prediction
) {
    val isHealthy = prediction.disease.lowercase().contains("healthy")
    val accentColor = if (isHealthy) Color(0xFF22C55E) else Color(0xFFEF4444)
    
    val animatedProgress by animateFloatAsState(
        targetValue = (prediction.confidence / 100f).toFloat(),
        animationSpec = tween(durationMillis = 1200)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI Diagnostics Result",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DIAGNOSIS CLASS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = prediction.disease,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confidence progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Confidence",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
                Text(
                    text = "${prediction.confidence}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fields list
            ResultFieldRow(
                icon = Icons.Default.Refresh,
                iconColor = Color(0xFF60A5FA),
                label = "Inference Speed",
                value = "${prediction.processingTimeMs} ms"
            )
            Spacer(modifier = Modifier.height(10.dp))
            ResultFieldRow(
                icon = Icons.Default.Settings,
                iconColor = Color(0xFF60A5FA),
                label = "Model",
                value = "YOLOv5 Segmentation"
            )
            Spacer(modifier = Modifier.height(10.dp))
            ResultFieldRow(
                icon = Icons.Default.Check,
                iconColor = accentColor,
                label = "Prediction Status",
                value = if (prediction.success) "Success" else "Failed"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // AI Recommendation Box (Futuristic Treatment Plan)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .padding(16.dp)
            ) {
                Text(
                    text = "AI TREATMENT RECOMMENDATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF60A5FA)
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                val recommendation = if (isHealthy) {
                    "Maintain current biosecurity protocols. Keep flock litter dry and ensure clean ventilation to sustain current flock status."
                } else {
                    "Quarantine affected birds immediately. Consult veterinarian to introduce specific antibiotics or electrolyte therapy. Clean feed trays with sanitizers."
                }
                Text(
                    text = recommendation,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ResultFieldRow(
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
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, color = Color(0xFF94A3B8))
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun CameraPreviewLayout(
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewView = it
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(previewView) {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView?.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }

        // Camera control buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
            }

            // Capture
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        val photoFile = File(
                            context.cacheDir,
                            "scan_${System.currentTimeMillis()}.jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val savedUri = Uri.fromFile(photoFile)
                                    onImageCaptured(savedUri)
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    }
            )

            // Placeholder to align capture button in center
            Spacer(modifier = Modifier.size(54.dp))
        }
    }
}
