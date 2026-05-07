package com.example.palmscanner.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.palmscanner.R
import com.example.palmscanner.databinding.FragmentResultBinding
import com.example.palmscanner.ui.base.BaseFragment
import com.example.palmscanner.ui.result.state.ResultUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ResultFragment : BaseFragment<FragmentResultBinding>() {

    private val viewModel: ResultViewModel by viewModels()
    private val args: ResultFragmentArgs by navArgs()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentResultBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadSession(args.session)

        binding.btnScanAgain.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_permission)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state -> renderState(state) }
        }
    }

    private fun renderState(state: ResultUiState) {
        when (state) {
            is ResultUiState.Loading -> Unit
            is ResultUiState.Error   -> Unit
            is ResultUiState.Success -> {
                val session = state.session

                // Palm image
                binding.ivPalm.load(File(session.palmImagePath))
                binding.tvHandSide.text = session.handSide.name

                // Status
                binding.tvStatus.text = if (session.isFullyValid)
                    "✅ Scan Complete" else "⚠️ Some fingers did not match"

                // Metadata
                val meta = session.palmMetadata
                binding.tvBrightness.text = "Brightness: ${meta.brightnessDisplay()}"
                binding.tvBlur.text       = "Blur Score: ${meta.blurDisplay()}"
                binding.tvFocus.text      = "Focus: ${meta.focusDisplay()}"
                binding.tvCamera.text     = "Camera: ${meta.cameraType.displayName}"
                binding.tvDeviceId.text   = "Device: ${meta.deviceId}"

                // Finger results RecyclerView
                binding.rvFingers.layoutManager = LinearLayoutManager(
                    requireContext(), LinearLayoutManager.HORIZONTAL, false
                )
                binding.rvFingers.adapter = FingerResultAdapter(session.fingerResults)
            }
        }
    }
}