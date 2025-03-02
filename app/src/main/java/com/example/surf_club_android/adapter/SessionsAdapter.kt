package com.example.surf_club_android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.surf_club_android.R
import com.example.surf_club_android.model.Post
import com.google.android.material.button.MaterialButton

class SessionsAdapter(private val sessions: List<Post>, private val onJoinClick: (Post) -> Unit) :
    RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.bind(session, onJoinClick)
    }

    override fun getItemCount(): Int = sessions.size

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.iv_host_profile)
        private val hostName: TextView = itemView.findViewById(R.id.tv_host_name)
        private val sessionDate: TextView = itemView.findViewById(R.id.tv_date)
        private val waveHeight: TextView = itemView.findViewById(R.id.tv_wave_height)
        private val windSpeed: TextView = itemView.findViewById(R.id.tv_wind_height)
        private val description: TextView = itemView.findViewById(R.id.tv_description)
        private val postImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        private val joinButton: MaterialButton = itemView.findViewById(R.id.btn_join)

        fun bind(session: Post, onJoinClick: (Post) -> Unit) {
            hostName.text = session.authorName
            sessionDate.text = session.sessionDate
            waveHeight.text = session.waveHeight
            windSpeed.text = session.windSpeed
            description.text = session.description

            Glide.with(itemView.context)
                .load(session.authorImage)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(profileImage)

            Glide.with(itemView.context)
                .load(session.postImage)
                .placeholder(R.drawable.ic_empty_sessions)
                .into(postImage)

            joinButton.setOnClickListener {
                onJoinClick(session)
            }
        }
    }
}
