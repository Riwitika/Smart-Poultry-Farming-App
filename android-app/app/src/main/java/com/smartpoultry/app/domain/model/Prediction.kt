package com.smartpoultry.app.domain.model

data class Prediction(
    val success: Boolean,
    val disease: String,
    val confidence: Double,
    val processingTimeMs: Int,
    val message: String
)
