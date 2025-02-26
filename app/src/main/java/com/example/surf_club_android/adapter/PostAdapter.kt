package com.example.surf_club_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surf_club_android.databinding.ItemPostBinding
import com.example.surf_club_android.model.Post

class PostAdapter : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                tvHostName.text = post.hostName
                tvDate.text = post.date
                tvWaveHeight.text = post.waveHeight
                tvWindHeight.text = post.windSpeed
                tvDescription.text = post.description

                Glide.with(ivHostProfile)
                    .load(post.hostProfileImage)
                    .circleCrop()
                    .into(ivHostProfile)

                Glide.with(ivPostImage)
                    .load(post.postImage)
                    .centerCrop()
                    .into(ivPostImage)

                btnJoin.setOnClickListener {
                    // TODO: Implement join functionality
                }

                btnParticipants.setOnClickListener {
                    // TODO: Implement participants functionality
                }
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}