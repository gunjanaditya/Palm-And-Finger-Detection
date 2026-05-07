package com.example.palmscanner.di

import android.content.Context
import com.example.palmscanner.camera.CameraManager
import com.example.palmscanner.data.camera.BlurAnalyzer
import com.example.palmscanner.data.camera.LuminosityAnalyzer
import com.example.palmscanner.data.ml.FingerExtensionChecker
import com.example.palmscanner.data.ml.HandLandmarkAnalyzer
import com.example.palmscanner.data.ml.PalmSideDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    @Provides
    @Singleton
    fun provideLuminosityAnalyzer(): LuminosityAnalyzer = LuminosityAnalyzer()

    @Provides
    @Singleton
    fun provideBlurAnalyzer(): BlurAnalyzer = BlurAnalyzer()

    @Provides
    @Singleton
    fun provideFingerExtensionChecker(): FingerExtensionChecker = FingerExtensionChecker()

    @Provides
    @Singleton
    fun providePalmSideDetector(): PalmSideDetector = PalmSideDetector()

    @Provides
    @Singleton
    fun provideHandLandmarkAnalyzer(
        @ApplicationContext context: Context,
        fingerExtensionChecker: FingerExtensionChecker,
        palmSideDetector: PalmSideDetector
    ): HandLandmarkAnalyzer = HandLandmarkAnalyzer(
        context                = context,
        fingerExtensionChecker = fingerExtensionChecker,
        palmSideDetector       = palmSideDetector
    )

    @Provides
    @Singleton
    fun provideCameraManager(
        @ApplicationContext context: Context,
        luminosityAnalyzer: LuminosityAnalyzer,
        blurAnalyzer: BlurAnalyzer,
        handLandmarkAnalyzer: HandLandmarkAnalyzer
    ): CameraManager = CameraManager(
        context              = context,
        luminosityAnalyzer   = luminosityAnalyzer,
        blurAnalyzer         = blurAnalyzer,
        handLandmarkAnalyzer = handLandmarkAnalyzer
    )
}