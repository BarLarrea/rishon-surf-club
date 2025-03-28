package com.example.surf_club_android.base

import com.example.surf_club_android.model.schemas.Post
import com.example.surf_club_android.model.schemas.User

typealias PostsCallback = (List<Post>) -> Unit
typealias EmptyCallback = () -> Unit
typealias SuccessCallback = (Boolean) -> Unit
typealias UsersCallback = (List<User>) -> Unit

object Constants {

    object COLLECTIONS {
        const val POSTS = "posts"
        const val USERS = "users"
    }
}