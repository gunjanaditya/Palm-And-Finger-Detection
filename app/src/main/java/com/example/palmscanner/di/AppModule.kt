package com.example.palmscanner.di

import android.content.Context
import com.example.palmscanner.utils.DeviceUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDeviceUtils(
        @ApplicationContext context: Context
    ): DeviceUtils = DeviceUtils(context)
}