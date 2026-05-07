package com.example.palmscanner.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.example.palmscanner.domain.model.CaptureMetadata
import com.example.palmscanner.domain.repository.IStorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IStorageRepository {

    companion object {
        private const val FOLDER_NAME = "Finger Data"
        private const val LOG_FILE_NAME = "capture_log.csv"
    }

    private fun getOutputFolder(): File {
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            FOLDER_NAME
        )
        if (!folder.exists()) folder.mkdirs()
        return folder
    }

    override suspend fun saveBitmap(bitmap: Bitmap, fileName: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val file = File(getOutputFolder(), fileName)
                val format = if (fileName.endsWith(".png"))
                    Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                FileOutputStream(file).use { out ->
                    bitmap.compress(format, 90, out)
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    override suspend fun saveMetadata(metadata: CaptureMetadata): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val file = File(getOutputFolder(), LOG_FILE_NAME)
                val exists = file.exists()
                
                FileOutputStream(file, true).use { out ->
                    if (!exists) {
                        // Header
                        val header = "Timestamp,DeviceID,CameraType,Brightness,Blur,FocusDistance\n"
                        out.write(header.toByteArray())
                    }
                    val line = "${metadata.timestamp},${metadata.deviceId},${metadata.cameraType}," +
                            "${metadata.brightnessScore},${metadata.blurScore},${metadata.focusDistance}\n"
                    out.write(line.toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    override fun buildPalmFileName(handSide: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${handSide}_Hand_$timestamp.png"
    }

    override fun buildFingerFileName(handSide: String, fingerName: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${handSide}_Hand_${fingerName}_Finger_$timestamp.jpg"
    }

    override fun isStorageAvailable(): Boolean =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}
