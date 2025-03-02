package com.example.surf_club_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String,
    var profileImageUrl: String? = null,
    var aboutMe: String? = null
) {
    constructor() : this("", "", "", "", "", "")
    companion object {
        private const val ID_KEY = "id"
        private const val FIRST_NAME_KEY = "firstName"
        private const val LAST_NAME_KEY = "lastName"
        private const val EMAIL_KEY = "email"
        private const val PASSWORD_KEY = "password"
        private const val ROLE_KEY = "role"
        private const val PROFILE_IMAGE_URL_KEY = "profileImageUrl"
        private const val ABOUT_ME_KEY = "aboutMe"
        fun fromJSON(json: Map<String, Any>): User {
            return User(
                id = json[ID_KEY] as? String ?: "",
                firstName = json[FIRST_NAME_KEY] as? String ?: "",
                lastName = json[LAST_NAME_KEY] as? String ?: "",
                email = json[EMAIL_KEY] as? String ?: "",
                password = json[PASSWORD_KEY] as? String ?: "",
                role = json[ROLE_KEY] as? String ?: "",
                profileImageUrl = json[PROFILE_IMAGE_URL_KEY] as? String ?: "",
                aboutMe = json[ABOUT_ME_KEY] as? String ?: ""
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            FIRST_NAME_KEY to firstName,
            LAST_NAME_KEY to lastName,
            EMAIL_KEY to email,
            PASSWORD_KEY to password,
            ROLE_KEY to role,
            PROFILE_IMAGE_URL_KEY to (profileImageUrl ?: ""),
            ABOUT_ME_KEY to (aboutMe ?: "")
        )
}