package com.example.surf_club_android.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.ItemPostBinding
import com.example.surf_club_android.model.Model
import com.example.surf_club_android.model.Post
import com.google.firebase.auth.FirebaseAuth

class PostAdapter(
    private val isProfileView: Boolean,
    private val onPostRemoved: ((String) -> Unit)? = null,
    private val onUpdate: ((Post) -> Unit)? = null
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        android.util.Log.d("PostAdapter", "Binding post at position $position")
        holder.bind(getItem(position), currentUserId, isProfileView, onPostRemoved, onUpdate)
    }

    class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            post: Post,
            currentUserId: String,
            isProfileView: Boolean,
            onPostRemoved: ((String) -> Unit)?,
            onUpdate: ((Post) -> Unit)?
        ) {
            binding.apply {
                // Fetch and display user details
                Model.shared.getUser(post.author) { user ->
                    tvHostName.text = user?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Author"
                    Glide.with(ivHostProfile.context)
                        .load(user?.profileImageUrl?.takeIf { it.isNotEmpty() } ?: R.drawable.user_profile_default)
                        .circleCrop()
                        .into(ivHostProfile)
                }

                tvDate.text = post.sessionDate
                tvWaveHeight.text = "${post.waveHeight} Meter"
                tvWindHeight.text = "${post.windSpeed} KM/H"
                tvDescription.text = post.description

                // Load post image (or a default)
                val postImageUrl = if (post.postImage.isNotEmpty()) post.postImage else R.drawable.main_logo
                Glide.with(ivPostImage.context)
                    .load(postImageUrl)
                    .centerCrop()
                    .into(ivPostImage)

                // Set join button text and color
                val isUserJoined = post.participants.contains(currentUserId)
                btnJoin.text = if (isUserJoined) "Leave" else "Join"
                btnJoin.setTextColor(
                    if (isUserJoined) itemView.context.getColor(R.color.red)
                    else itemView.context.getColor(R.color.white)
                )
                btnJoin.setOnClickListener {
                    Model.shared.toggleSessionParticipation(currentUserId, post.id) { success, isJoining ->
                        if (success) {
                            btnJoin.text = if (isJoining) "Leave" else "Join"
                            btnJoin.setTextColor(
                                if (isJoining) itemView.context.getColor(R.color.red)
                                else itemView.context.getColor(R.color.white)
                            )
                            if (isProfileView && !isJoining) {
                                onPostRemoved?.invoke(post.id)
                            }
                        }
                    }
                }

                btnParticipants.setBackgroundColor(itemView.context.getColor(R.color.blue_primary))
                btnParticipants.setTextColor(itemView.context.getColor(R.color.white))
                btnParticipants.setOnClickListener {
                    // TODO: Implement participants functionality
                }
                btnJoin.visibility = View.VISIBLE

                // Show update and delete buttons only if the current user is the author
                if (currentUserId == post.author) {
                    btnUpdate.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE

                    btnUpdate.setOnClickListener {
                        android.util.Log.e("PostAdapter", "Clicked on update")
                        onUpdate?.invoke(post)
                    }
                    btnDelete.setOnClickListener {
                        Model.shared.deletePost(post.id) { success ->
                            if (success) {
                                onPostRemoved?.invoke(post.id)
                            } else {
                                // Optionally handle deletion failure
                            }
                        }
                    }
                } else {
                    btnUpdate.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}
