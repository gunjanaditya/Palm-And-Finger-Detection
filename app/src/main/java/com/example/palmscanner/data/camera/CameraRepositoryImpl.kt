package com.example.palmscanner.data.camera

import com.example.palmscanner.camera.CameraManager
import com.example.palmscanner.domain.model.CaptureMetadata
import com.example.palmscanner.domain.model.enums.CameraType
import com.example.palmscanner.domain.repository.ICameraRepository
import com.example.palmscanner.utils.DeviceUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

class CameraRepositoryImpl @Inject constructor(
    private val cameraManager: CameraManager,
    private val deviceUtils: DeviceUtils
) : ICameraRepository {

    override val focusDistance: Flow<Float> = cameraManager.focusDistance

    override suspend fun captureImage(fileName: String): String? =
        suspendCancellableCoroutine { cont ->
            val file = File(fileName)
            cameraManager.captureImage(
                outputFile = file,
                onSuccess  = { path -> cont.resume(path) },
                onError    = { cont.resume(null) }
            )
        }

    override suspend fun buildCaptureMetadata(
        brightnessScore: Double,
        blurScore: Double,
        focusDistance: Float
    ): CaptureMetadata = CaptureMetadata(
        brightnessScore = brightnessScore,
        blurScore       = blurScore,
        focusDistance   = focusDistance,
        cameraType      = cameraManager.cameraType.value,
        deviceId        = deviceUtils.getDeviceId(),
        timestamp       = System.currentTimeMillis()
    )
}