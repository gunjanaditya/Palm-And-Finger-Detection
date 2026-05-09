package com.example.palmscanner.domain.repository

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.CaptureMetadata

interface IStorageRepository {

    suspend fun saveBitmap(bitmap: Bitmap, fileName: String): String?
    suspend fun saveMetadata(metadata: CaptureMetadata): Boolean
    fun buildPalmFileName(handSide: String): String
    fun buildFingerFileName(handSide: String, fingerName: String): String
    fun isStorageAvailable(): Boolean
}