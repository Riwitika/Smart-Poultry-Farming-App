package com.smartpoultry.app.data.repository

import com.smartpoultry.app.data.remote.ApiService
import com.smartpoultry.app.domain.model.Prediction
import com.smartpoultry.app.domain.repository.PredictionRepository
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PredictionRepository {
    override suspend fun getPrediction(file: MultipartBody.Part): Result<Prediction> {
        return try {
            val response = apiService.predict(file)
            Result.success(
                Prediction(
                    success = response.success,
                    disease = response.prediction,
                    confidence = response.confidence,
                    processingTimeMs = response.processingTimeMs,
                    message = response.message
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
