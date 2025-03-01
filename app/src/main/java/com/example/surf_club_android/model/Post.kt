package com.example.surf_club_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String,
    val author: String = "",      // The user ID of the author
    val authorName: String = "",  // Updated after fetching the user details
    val authorImage: String = "", // Updated after fetching the user details
    val date: String,
    val waveHeight: String,
    val windSpeed: String,
    val description: String,
    val postImage: String
) {
    companion object {
        private const val ID_KEY = "id"
        private const val AUTHOR_KEY = "author"
        private const val NAME_KEY = "name"
        private const val IMAGE_KEY = "image"
        private const val DATE_KEY = "date"
        private const val WAVE_HEIGHT_KEY = "waveHeight"
        private const val WIND_SPEED_KEY = "windSpeed"
        private const val DESCRIPTION_KEY = "description"
        private const val POST_IMAGE_KEY = "postImage"

        fun fromJSON(json: Map<String, Any>): Post {
            return Post(
                id = json[ID_KEY] as? String ?: "",
                author = json[AUTHOR_KEY] as? String ?: "",
                authorName = json[NAME_KEY] as? String ?: "",
                authorImage = json[IMAGE_KEY] as? String ?: "",
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
            AUTHOR_KEY to author,
            NAME_KEY to authorName,
            IMAGE_KEY to authorImage,
            DATE_KEY to date,
            WAVE_HEIGHT_KEY to waveHeight,
            WIND_SPEED_KEY to windSpeed,
            DESCRIPTION_KEY to description,
            POST_IMAGE_KEY to postImage
        )
}
