package com.example.mforum.ui.teaching

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mforum.databinding.ItemVideoResourceBinding
import com.example.mforum.network.TeachingResource

class VideoResourceAdapter(
    private val onItemClick: (TeachingResource) -> Unit
) : ListAdapter<TeachingResource, VideoResourceAdapter.VideoViewHolder>(VideoDiffCallback()) {

    class VideoViewHolder(private val binding: ItemVideoResourceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(resource: TeachingResource) {
            binding.titleTextView.text = resource.title
            binding.descriptionTextView.text = resource.description
            binding.durationTextView.text = resource.duration
            binding.levelTextView.text = resource.level

            // 使用Glide加载缩略图
            Glide.with(binding.root.context)
                .load(resource.thumbnailUrl)
                .placeholder(com.example.mforum.R.drawable.placeholder_video)
                .into(binding.thumbnailImageView)

            binding.root.setOnClickListener {
                // 注意：这里我们稍后会修复onItemClick的调用
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoResourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val resource = getItem(position)
        holder.bind(resource)
        holder.itemView.setOnClickListener {
            onItemClick(resource)
        }
    }
}

class VideoDiffCallback : DiffUtil.ItemCallback<TeachingResource>() {
    override fun areItemsTheSame(oldItem: TeachingResource, newItem: TeachingResource): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TeachingResource, newItem: TeachingResource): Boolean {
        return oldItem == newItem
    }
}