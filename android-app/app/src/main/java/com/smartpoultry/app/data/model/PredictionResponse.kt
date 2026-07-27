package com.smartpoultry.app.data.model

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("prediction") val prediction: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("processing_time_ms") val processingTimeMs: Int,
    @SerializedName("message") val message: String
)
