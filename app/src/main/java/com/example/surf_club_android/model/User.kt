package com.example.surf_club_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    val id:String,
    val firstName:String,
    val lastName:String,
    val email:String,
    val password:String,
    val role:String,
)
