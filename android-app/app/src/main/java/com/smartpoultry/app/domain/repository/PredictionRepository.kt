package com.smartpoultry.app.domain.repository

import com.smartpoultry.app.domain.model.Prediction
import okhttp3.MultipartBody

interface PredictionRepository {
    suspend fun getPrediction(file: MultipartBody.Part): Result<Prediction>
}
