package com.example.surf_club_android.model.repository

import com.example.surf_club_android.model.FirebaseModel
import com.example.surf_club_android.model.User
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository {
    private val firebaseModel = FirebaseModel()

    suspend fun getUserById(id: String): User? = suspendCoroutine { continuation ->
        firebaseModel.getUser(id) { user ->
            continuation.resume(user)
        }
    }

    suspend fun updateUser(
        firebaseUser: FirebaseUser,
        user: User
    ): Boolean = suspendCoroutine { continuation ->
        firebaseModel.saveUser(
            user = firebaseUser,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            role = user.role,
            image = user.profileImageUrl
        ) { success, _ ->
            continuation.resume(success)
        }
    }
}