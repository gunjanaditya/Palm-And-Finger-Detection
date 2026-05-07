package com.example.palmscanner.di

import com.example.palmscanner.data.camera.CameraRepositoryImpl
import com.example.palmscanner.data.ml.HandDetectionRepositoryImpl
import com.example.palmscanner.data.storage.StorageRepositoryImpl
import com.example.palmscanner.data.validation.ValidationRepositoryImpl
import com.example.palmscanner.domain.repository.ICameraRepository
import com.example.palmscanner.domain.repository.IHandDetectionRepository
import com.example.palmscanner.domain.repository.IStorageRepository
import com.example.palmscanner.domain.repository.IValidationRepository
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
    abstract fun bindCameraRepository(
        impl: CameraRepositoryImpl
    ): ICameraRepository

    @Binds
    @Singleton
    abstract fun bindHandDetectionRepository(
        impl: HandDetectionRepositoryImpl
    ): IHandDetectionRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        impl: StorageRepositoryImpl
    ): IStorageRepository

    @Binds
    @Singleton
    abstract fun bindValidationRepository(
        impl: ValidationRepositoryImpl
    ): IValidationRepository
}