package com.example.surf_club_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String,
    val hostName: String,
    val hostProfileImage: String,
    val date: String,
    val waveHeight: String,
    val windSpeed: String,
    val description: String,
    val postImage: String

)
