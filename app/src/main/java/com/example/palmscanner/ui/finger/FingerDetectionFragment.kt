package com.example.palmscanner.ui.finger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.palmscanner.camera.CameraManager
import com.example.palmscanner.databinding.FragmentFingerDetectionBinding
import com.example.palmscanner.domain.model.CaptureMetadata
import com.example.palmscanner.ui.base.BaseFragment
import com.example.palmscanner.ui.finger.state.FingerUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FingerDetectionFragment : BaseFragment<FragmentFingerDetectionBinding>() {

    private val viewModel: FingerDetectionViewModel by viewModels()
    private val args: FingerDetectionFragmentArgs by navArgs()

    @Inject lateinit var cameraManager: CameraManager

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFingerDetectionBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.palmImagePath = args.palmImagePath
        viewModel.expectedHand  = args.handSide

        cameraManager.bindCamera(viewLifecycleOwner, binding.previewView)
        viewModel.startDetection()

        binding.btnCapture.setOnClickListener {
            val bitmap = binding.previewView.bitmap ?: return@setOnClickListener
            viewModel.captureFinger(bitmap)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state -> renderState(state) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detectionResult.collect { result ->
                val finger = viewModel.getCurrentFinger()
                binding.fingerOvalOverlay.updateState(
                    detected = result.isDetected,
                    label    = finger?.displayName ?: "FINGER"
                )
                binding.tvLightLevel.text = result.lightLevel.displayLabel
                binding.tvBlurStatus.text = if (result.isBlurry) "Blurry ⚠️" else "Sharp ✅"
            }
        }
    }

    private fun renderState(state: FingerUiState) {
        when (state) {
            is FingerUiState.Idle,
            is FingerUiState.WaitingForFinger -> {
                binding.btnCapture.isEnabled  = false
                binding.tvError.visibility    = View.GONE
                val finger = (state as? FingerUiState.WaitingForFinger)?.fingerName
                binding.tvInstruction.text    =
                    "Place your ${finger?.displayName ?: ""} finger in the oval"
                binding.tvProgress.text       =
                    "Finger ${viewModel.getCurrentIndex() + 1} of 5"
            }
            is FingerUiState.FingerDetected,
            is FingerUiState.ReadyToCapture -> {
                binding.btnCapture.isEnabled = true
                binding.tvError.visibility   = View.GONE
            }
            is FingerUiState.ValidationFailed -> {
                binding.btnCapture.isEnabled = false
                binding.tvError.text         = state.reason
                binding.tvError.visibility   = View.VISIBLE
            }
            is FingerUiState.FingerCaptured -> {
                binding.tvProgress.text =
                    "Finger ${viewModel.getCurrentIndex() + 1} of 5"
                binding.tvError.visibility = View.GONE
            }
            is FingerUiState.AllFingersComplete -> {
                val session = viewModel.buildScanSession(
                    CaptureMetadata(
                        brightnessScore = viewModel.detectionResult.value.brightnessScore,
                        blurScore       = viewModel.detectionResult.value.blurScore,
                        focusDistance   = viewModel.detectionResult.value.focusDistance,
                        cameraType      = cameraManager.cameraType.value,
                        deviceId        = ""
                    )
                )
                val action = FingerDetectionFragmentDirections
                    .actionFingerToResult(session = session)
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}