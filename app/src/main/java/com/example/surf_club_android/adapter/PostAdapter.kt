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
import androidx.core.os.bundleOf
import androidx.navigation.NavController

class PostAdapter(
    private val navController: NavController,
    private val isProfileView: Boolean,
    private val onPostRemoved: ((String) -> Unit)? = null
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, navController)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        holder.bind(getItem(position), currentUserId, isProfileView, onPostRemoved)
    }

    class PostViewHolder(private val binding: ItemPostBinding, private val navController: NavController)
        : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(post: Post, currentUserId: String, isProfileView: Boolean, onPostRemoved: ((String) -> Unit)?) {
            binding.apply {
                // Fetch user details
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

                // Load post image (fallback to default if empty)
                val postImageUrl = post.postImage.takeIf { it.isNotEmpty() } ?: R.drawable.main_logo
                Glide.with(ivPostImage.context)
                    .load(postImageUrl)
                    .centerCrop()
                    .into(ivPostImage)

                val isUserJoined = post.participants.contains(currentUserId)

                // Set button appearance
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

                            // Remove post from profile view if user leaves
                            if (isProfileView && !isJoining) {
                                onPostRemoved?.invoke(post.id)
                            }
                        }
                    }
                }

                // Participants button styling
                btnParticipants.setBackgroundColor(itemView.context.getColor(R.color.blue_primary))
                btnParticipants.setTextColor(itemView.context.getColor(R.color.white))

                btnParticipants.setOnClickListener {
                    val bundle = bundleOf("postId" to post.id)

                    val actionId = when {
                        isProfileView -> R.id.action_profileFragment_to_participantsFragment
                        else -> R.id.action_homeFragment_to_participantsFragment
                    }

                    navController.navigate(actionId, bundle)
                }

                // Ensure join button is always visible
                btnJoin.visibility = View.VISIBLE
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
