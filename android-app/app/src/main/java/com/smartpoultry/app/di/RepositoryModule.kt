package com.smartpoultry.app.di

import com.smartpoultry.app.data.repository.PredictionRepositoryImpl
import com.smartpoultry.app.domain.repository.PredictionRepository
import dagger.Binds
import dagger.Module
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
}
