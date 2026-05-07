package com.example.palmscanner.ui.palm

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.palmscanner.camera.CameraManager
import com.example.palmscanner.databinding.FragmentPalmDetectionBinding
import com.example.palmscanner.domain.model.enums.LightLevel
import com.example.palmscanner.domain.model.enums.PalmSide
import com.example.palmscanner.ui.base.BaseFragment
import com.example.palmscanner.ui.palm.state.PalmUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PalmDetectionFragment : BaseFragment<FragmentPalmDetectionBinding>() {

    private val viewModel: PalmDetectionViewModel by viewModels()

    @Inject lateinit var cameraManager: CameraManager

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPalmDetectionBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraManager.bindCamera(viewLifecycleOwner, binding.previewView)
        viewModel.startDetection()

        binding.btnCapture.setOnClickListener {
            val bitmap = binding.previewView.bitmap ?: return@setOnClickListener
            viewModel.capturePalm(bitmap)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state -> renderState(state) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detectionResult.collect { result ->
                binding.palmOverlay.updateState(
                    detected = result.isDetected,
                    ready    = result.isDetected && result.palmSide == PalmSide.PALM
                )
                binding.tvHandSide.text    = result.handSide.name
                binding.tvPalmSide.text    = result.palmSide.name
                binding.tvFingerCount.text = "${result.fingerCount}/5"
                binding.tvLightLevel.text  = result.lightLevel.displayLabel
                binding.tvBlurStatus.text  = if (result.isBlurry) "Blurry ⚠️" else "Sharp ✅"
            }
        }
    }

    private fun renderState(state: PalmUiState) {
        when (state) {
            is PalmUiState.Idle,
            is PalmUiState.Detecting -> {
                binding.btnCapture.isEnabled = false
                binding.tvError.visibility   = View.GONE
            }
            is PalmUiState.ReadyToCapture -> {
                binding.btnCapture.isEnabled = true
                binding.tvError.visibility   = View.GONE
            }
            is PalmUiState.Captured -> {
                val action = PalmDetectionFragmentDirections
                    .actionPalmToFinger(
                        handSide      = viewModel.detectionResult.value.handSide,
                        palmImagePath = state.imagePath
                    )
                findNavController().navigate(action)
            }
            is PalmUiState.Error -> {
                binding.btnCapture.isEnabled = false
                binding.tvError.text         = state.message
                binding.tvError.visibility   = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}