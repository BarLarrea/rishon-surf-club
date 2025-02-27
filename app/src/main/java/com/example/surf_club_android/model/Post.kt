package com.example.surf_club_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

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
) {
    companion object {
        private const val ID_KEY = "id"
        private const val HOST_NAME_KEY = "hostName"
        private const val HOST_PROFILE_IMAGE_KEY = "hostProfileImage"
        private const val DATE_KEY = "date"
        private const val WAVE_HEIGHT_KEY = "waveHeight"
        private const val WIND_SPEED_KEY = "windSpeed"
        private const val DESCRIPTION_KEY = "description"
        private const val POST_IMAGE_KEY = "postImage"

        fun fromJSON(json: Map<String, Any>): Post {
            return Post(
                id = json[ID_KEY] as? String ?: "",
                hostName = json[HOST_NAME_KEY] as? String ?: "",
                hostProfileImage = json[HOST_PROFILE_IMAGE_KEY] as? String ?: "",
                date = json[DATE_KEY] as? String ?: "",
                waveHeight = json[WAVE_HEIGHT_KEY] as? String ?: "",
                windSpeed = json[WIND_SPEED_KEY] as? String ?: "",
                description = json[DESCRIPTION_KEY] as? String ?: "",
                postImage = json[POST_IMAGE_KEY] as? String ?: ""
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            HOST_NAME_KEY to hostName,
            HOST_PROFILE_IMAGE_KEY to hostProfileImage,
            DATE_KEY to date,
            WAVE_HEIGHT_KEY to waveHeight,
            WIND_SPEED_KEY to windSpeed,
            DESCRIPTION_KEY to description,
            POST_IMAGE_KEY to postImage
        )
}