package com.example.surf_club_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String,
    val description: String,
    val image: String,
    val date: String,
    val userId: String,

)
