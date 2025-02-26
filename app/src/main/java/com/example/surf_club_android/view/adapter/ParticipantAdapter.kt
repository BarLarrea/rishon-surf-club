
package com.example.surf_club_android.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surf_club_android.R
import com.example.surf_club_android.model.User

class ParticipantAdapter : RecyclerView.Adapter<ParticipantAdapter.ViewHolder>() {

    private var participants: List<User> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setParticipants(newParticipants: List<User>) {
        participants = newParticipants
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.participantImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.participantNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = participants[position]
        holder.nameTextView.text = "${user.firstName} ${user.lastName}"
        // כאן תצטרך להשתמש בספריית טעינת תמונות כמו Glide או Picasso
        // לדוגמה: Glide.with(holder.itemView).load(user.profileImageUrl).into(holder.imageView)
    }

    override fun getItemCount() = participants.size
}