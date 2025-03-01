package com.example.surf_club_android.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.surf_club_android.databinding.ItemParticipantBinding
import com.example.surf_club_android.model.User

class ParticipantAdapter : RecyclerView.Adapter<ParticipantAdapter.ViewHolder>() {

    private var participants: List<User> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun updateParticipants(newParticipants: List<User>) {
        participants = newParticipants
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.participantNameTextView.text = user.firstName
            // TODO: Load user image if available
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participants[position])
    }

    override fun getItemCount() = participants.size
}