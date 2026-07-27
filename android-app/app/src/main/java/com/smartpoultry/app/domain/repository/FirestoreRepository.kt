package com.smartpoultry.app.domain.repository

import android.net.Uri
import com.smartpoultry.app.domain.model.PredictionRecord
import com.smartpoultry.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    fun getUserProfile(uid: String): Flow<UserProfile?>
    fun getPredictions(uid: String): Flow<List<PredictionRecord>>
    suspend fun createUserProfile(userProfile: UserProfile): Result<Unit>
    suspend fun savePrediction(predictionRecord: PredictionRecord): Result<Unit>
    suspend fun deletePrediction(predictionId: String): Result<Unit>
    suspend fun uploadPredictionImage(uri: Uri, uid: String): Result<String>
}
