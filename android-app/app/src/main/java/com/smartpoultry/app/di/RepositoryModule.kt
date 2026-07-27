package com.smartpoultry.app.di

import com.google.firebase.firestore.FirebaseFirestore
import com.smartpoultry.app.data.repository.FirestoreRepositoryImpl
import com.smartpoultry.app.data.repository.PredictionRepositoryImpl
import com.smartpoultry.app.domain.repository.FirestoreRepository
import com.smartpoultry.app.domain.repository.PredictionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPredictionRepository(
        predictionRepositoryImpl: PredictionRepositoryImpl
    ): PredictionRepository

    @Binds
    @Singleton
    abstract fun bindFirestoreRepository(
        firestoreRepositoryImpl: FirestoreRepositoryImpl
    ): FirestoreRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }
    }
}
