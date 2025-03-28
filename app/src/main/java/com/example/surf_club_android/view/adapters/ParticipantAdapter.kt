package com.example.surf_club_android.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surf_club_android.R
import com.example.surf_club_android.model.schemas.User

class ParticipantAdapter(
    private val onItemClick: (User) -> Unit
) : ListAdapter<User, ParticipantAdapter.ParticipantViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participant, parent, false)
        return ParticipantViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    class ParticipantViewHolder(
        itemView: View,
        private val onItemClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameText: TextView = itemView.findViewById(R.id.participantNameTextView)
        private val profileImage: ImageView = itemView.findViewById(R.id.participantImageView)

        @SuppressLint("SetTextI18n")
        fun bind(user: User) {
            nameText.text = "${user.firstName} ${user.lastName}"

            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(profileImage)

            itemView.setOnClickListener {
                onItemClick(user)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}

