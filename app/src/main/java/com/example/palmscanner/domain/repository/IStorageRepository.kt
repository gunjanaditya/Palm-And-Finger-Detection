package com.example.palmscanner.domain.repository

import android.graphics.Bitmap
import com.example.palmscanner.domain.model.CaptureMetadata

/**
 * Contract for all file I/O operations.
 * Implementation lives in data/storage/StorageRepositoryImpl.kt
 */
interface IStorageRepository {

    /**
     * Saves a bitmap to the "Finger Data" folder in external storage.
     * @param bitmap    The image to save
     * @param fileName  Full file name including extension
     *                  e.g. "Left_Hand_Thumb_Finger_1718000000000.jpg"
     * @return Absolute path of saved file, or null on failure.
     */
    suspend fun saveBitmap(bitmap: Bitmap, fileName: String): String?

    /**
     * Saves capture metadata to a log file in the "Finger Data" folder.
     * Fulfills requirement (s).
     */
    suspend fun saveMetadata(metadata: CaptureMetadata): Boolean

    /**
     * Generates the correct file name based on naming convention.
     * Palm:   Left_Hand_1718000000000.png
     * Finger: Left_Hand_Index_Finger_1718000000000.jpg
     */
    fun buildPalmFileName(handSide: String): String
    fun buildFingerFileName(handSide: String, fingerName: String): String

    /**
     * Checks if external storage is writable.
     */
    fun isStorageAvailable(): Boolean
}