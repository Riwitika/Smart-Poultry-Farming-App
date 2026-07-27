package com.smartpoultry.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.smartpoultry.app.domain.model.PredictionRecord
import com.smartpoultry.app.domain.model.UserProfile
import com.smartpoultry.app.domain.repository.FirestoreRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreRepository {

    override fun getUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = UserProfile(
                    uid = snapshot.getString("uid").orEmpty(),
                    fullName = snapshot.getString("fullName").orEmpty(),
                    email = snapshot.getString("email").orEmpty(),
                    createdAt = snapshot.getLong("createdAt") ?: 0L,
                    totalPredictions = snapshot.getLong("totalPredictions")?.toInt() ?: 0,
                    healthyPredictions = snapshot.getLong("healthyPredictions")?.toInt() ?: 0,
                    diseasePredictions = snapshot.getLong("diseasePredictions")?.toInt() ?: 0,
                    lastPrediction = snapshot.getLong("lastPrediction")
                )
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getPredictions(uid: String): Flow<List<PredictionRecord>> = callbackFlow {
        val colRef = firestore.collection("predictions")
            .whereEqualTo("uid", uid)
        
        val subscription = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.map { doc ->
                    PredictionRecord(
                        predictionId = doc.getString("predictionId").orEmpty(),
                        uid = doc.getString("uid").orEmpty(),
                        imageUrl = doc.getString("imageUrl").orEmpty(),
                        diseaseName = doc.getString("diseaseName").orEmpty(),
                        confidence = doc.getDouble("confidence") ?: 0.0,
                        processingTime = doc.getLong("processingTime")?.toInt() ?: 0,
                        modelName = doc.getString("modelName").orEmpty(),
                        predictionStatus = doc.getString("predictionStatus").orEmpty(),
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }.sortedByDescending { it.createdAt }
                trySend(list)
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun createUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            val map = hashMapOf(
                "uid" to userProfile.uid,
                "fullName" to userProfile.fullName,
                "email" to userProfile.email,
                "createdAt" to userProfile.createdAt,
                "totalPredictions" to userProfile.totalPredictions,
                "healthyPredictions" to userProfile.healthyPredictions,
                "diseasePredictions" to userProfile.diseasePredictions,
                "lastPrediction" to userProfile.lastPrediction
            )
            firestore.collection("users").document(userProfile.uid).set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePrediction(predictionRecord: PredictionRecord): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. Save prediction
                val predRef = firestore.collection("predictions").document(predictionRecord.predictionId)
                val predMap = hashMapOf(
                    "predictionId" to predictionRecord.predictionId,
                    "uid" to predictionRecord.uid,
                    "imageUrl" to predictionRecord.imageUrl,
                    "diseaseName" to predictionRecord.diseaseName,
                    "confidence" to predictionRecord.confidence,
                    "processingTime" to predictionRecord.processingTime,
                    "modelName" to predictionRecord.modelName,
                    "predictionStatus" to predictionRecord.predictionStatus,
                    "createdAt" to predictionRecord.createdAt
                )
                transaction.set(predRef, predMap)

                // 2. Update user stats
                val userRef = firestore.collection("users").document(predictionRecord.uid)
                val userSnap = transaction.get(userRef)
                if (userSnap.exists()) {
                    val currentTotal = userSnap.getLong("totalPredictions") ?: 0L
                    val currentHealthy = userSnap.getLong("healthyPredictions") ?: 0L
                    val currentDiseased = userSnap.getLong("diseasePredictions") ?: 0L

                    val isHealthy = predictionRecord.diseaseName.lowercase().contains("healthy")
                    val newHealthy = if (isHealthy) currentHealthy + 1 else currentHealthy
                    val newDiseased = if (!isHealthy) currentDiseased + 1 else currentDiseased

                    transaction.update(userRef, mapOf(
                        "totalPredictions" to currentTotal + 1,
                        "healthyPredictions" to newHealthy,
                        "diseasePredictions" to newDiseased,
                        "lastPrediction" to predictionRecord.createdAt
                    ))
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePrediction(predictionId: String): Result<Unit> {
        return try {
            val predRef = firestore.collection("predictions").document(predictionId)
            val snapshot = predRef.get().await()
            if (snapshot.exists()) {
                val uid = snapshot.getString("uid").orEmpty()
                val diseaseName = snapshot.getString("diseaseName").orEmpty()
                
                firestore.runTransaction { transaction ->
                    // 1. Delete prediction
                    transaction.delete(predRef)
                    
                    // 2. Decrement user stats
                    val userRef = firestore.collection("users").document(uid)
                    val userSnap = transaction.get(userRef)
                    if (userSnap.exists()) {
                        val currentTotal = userSnap.getLong("totalPredictions") ?: 0L
                        val currentHealthy = userSnap.getLong("healthyPredictions") ?: 0L
                        val currentDiseased = userSnap.getLong("diseasePredictions") ?: 0L

                        val isHealthy = diseaseName.lowercase().contains("healthy")
                        val newHealthy = if (isHealthy) maxOf(0L, currentHealthy - 1) else currentHealthy
                        val newDiseased = if (!isHealthy) maxOf(0L, currentDiseased - 1) else currentDiseased

                        transaction.update(userRef, mapOf(
                            "totalPredictions" to maxOf(0L, currentTotal - 1),
                            "healthyPredictions" to newHealthy,
                            "diseasePredictions" to newDiseased
                        ))
                    }
                }.await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
