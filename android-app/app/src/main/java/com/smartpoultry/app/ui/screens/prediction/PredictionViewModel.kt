package com.smartpoultry.app.ui.screens.prediction

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartpoultry.app.domain.model.Prediction
import com.smartpoultry.app.domain.model.PredictionRecord
import com.smartpoultry.app.domain.repository.PredictionRepository
import com.smartpoultry.app.domain.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import javax.inject.Inject

sealed interface PredictionUiState {
    object Idle : PredictionUiState
    object Loading : PredictionUiState
    data class Success(val prediction: Prediction) : PredictionUiState
    data class Error(val message: String) : PredictionUiState
}

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val repository: PredictionRepository,
    private val firestoreRepository: FirestoreRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<PredictionUiState>(PredictionUiState.Idle)
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    private val _firestoreError = MutableStateFlow<String?>(null)
    val firestoreError: StateFlow<String?> = _firestoreError.asStateFlow()

    fun uploadImage(uri: Uri) {
        _uiState.value = PredictionUiState.Loading

        viewModelScope.launch {
            val part = uriToMultipart(uri)
            if (part == null) {
                _uiState.value = PredictionUiState.Error("Failed to process selected image")
                return@launch
            }

            repository.getPrediction(part)
                .onSuccess { prediction ->
                    // Auto-save prediction to Firestore
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val record = PredictionRecord(
                            predictionId = UUID.randomUUID().toString(),
                            uid = uid,
                            imageUrl = uri.toString(),
                            diseaseName = prediction.disease,
                            confidence = prediction.confidence,
                            processingTime = prediction.processingTimeMs,
                            modelName = "YOLOv5 Segmentation",
                            predictionStatus = "Success",
                            createdAt = System.currentTimeMillis()
                        )
                        savePredictionToFirestoreWithRetry(record, prediction)
                    } else {
                        _uiState.value = PredictionUiState.Success(prediction)
                    }
                }
                .onFailure { exception ->
                    val errorMsg = when (exception) {
                        is UnknownHostException -> "No internet connection: ${exception.message}"
                        is SocketTimeoutException -> "Connection timeout: ${exception.message}"
                        is IOException -> "Network IO error: ${exception.message}"
                        else -> "${exception.javaClass.simpleName}: ${exception.message ?: "An unexpected error occurred"}"
                    }
                    _uiState.value = PredictionUiState.Error(errorMsg)
                }
        }
    }

    private fun savePredictionToFirestoreWithRetry(
        record: PredictionRecord,
        prediction: Prediction,
        retryCount: Int = 1
    ) {
        viewModelScope.launch {
            firestoreRepository.savePrediction(record)
                .onSuccess {
                    _uiState.value = PredictionUiState.Success(prediction)
                }
                .onFailure { exception ->
                    if (retryCount > 0) {
                        // Retry automatically once
                        savePredictionToFirestoreWithRetry(record, prediction, retryCount - 1)
                    } else {
                        // Show results, but trigger error snackbar for sync issues
                        _uiState.value = PredictionUiState.Success(prediction)
                        _firestoreError.value = "Failed to sync prediction with cloud: ${exception.message}"
                    }
                }
        }
    }

    fun resetState() {
        _uiState.value = PredictionUiState.Idle
    }

    fun clearFirestoreError() {
        _firestoreError.value = null
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        val file = uriToFile(uri) ?: return null
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    private fun uriToFile(uri: Uri): File? {
        val contentResolver = context.contentResolver ?: return null
        val file = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
