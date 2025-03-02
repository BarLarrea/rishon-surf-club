package com.example.surf_club_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.ItemPostBinding
import com.example.surf_club_android.model.Model
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
                // Use getUserById to fetch user details
                Model.shared.getUser(post.author) { user ->
                    if (user != null) {
                        tvHostName.text = "${user.firstName} ${user.lastName}"

                        // Load user profile image or default image
                        Glide.with(ivHostProfile.context)
                            .load(user.profileImageUrl.takeIf { !it.isNullOrEmpty() }
                                ?: R.drawable.user_profile_default)
                            .circleCrop()
                            .into(ivHostProfile)
                    } else {
                        tvHostName.text = "Unknown Author"

                        // Load default image for unknown user
                        Glide.with(ivHostProfile.context)
                            .load(R.drawable.user_profile_default)
                            .circleCrop()
                            .into(ivHostProfile)
                    }
                }

                tvDate.text = post.sessionDate
                tvWaveHeight.text = post.waveHeight + " Meter"
                tvWindHeight.text = post.windSpeed + " KM/H"
                tvDescription.text = post.description

                Glide.with(ivPostImage.context)
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