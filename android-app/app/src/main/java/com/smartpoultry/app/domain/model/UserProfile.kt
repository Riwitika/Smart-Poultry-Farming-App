package com.smartpoultry.app.domain.model

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val createdAt: Long,
    val totalPredictions: Int = 0,
    val healthyPredictions: Int = 0,
    val diseasePredictions: Int = 0,
    val lastPrediction: Long? = null
)
