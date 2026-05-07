package com.example.palmscanner.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.palmscanner.databinding.ItemFingerResultBinding
import com.example.palmscanner.domain.model.FingerCaptureResult
import java.io.File

class FingerResultAdapter(
    private val items: List<FingerCaptureResult>
) : RecyclerView.Adapter<FingerResultAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFingerResultBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemFingerResultBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.ivFinger.load(File(item.imagePath))
        holder.binding.tvFingerName.text  = item.fingerName.displayName
        holder.binding.tvMatchStatus.text = if (item.isValid) "✅" else "❌"
        holder.binding.tvScore.text       = "%.0f%%".format(item.matchScore * 100)
    }
}