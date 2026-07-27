package com.smartpoultry.app.domain.model

data class PredictionRecord(
    val predictionId: String,
    val uid: String,
    val imageUrl: String,
    val diseaseName: String,
    val confidence: Double,
    val processingTime: Int,
    val modelName: String,
    val predictionStatus: String,
    val createdAt: Long
)
