package com.example.surf_club_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.surf_club_android.model.dao.Converters

@Entity
@TypeConverters(Converters::class)
data class Post(
    @PrimaryKey val id: String,
    val author: String = "",
    val authorName: String = "",
    val authorImage: String = "",
    val date: String,
    val sessionDate: String,
    val waveHeight: String,
    val windSpeed: String,
    val description: String,
    val postImage: String,
    var participants: List<String> = emptyList()
)

{
    constructor() : this("", "", "", "", "", "", "", "", "", "", emptyList())

    companion object {
        private const val ID_KEY = "id"
        private const val AUTHOR_KEY = "author"
        private const val NAME_KEY = "name"
        private const val IMAGE_KEY = "image"
        private const val DATE_KEY = "date"
        private const val SESSION_DATE_KEY = "sessionDate"
        private const val WAVE_HEIGHT_KEY = "waveHeight"
        private const val WIND_SPEED_KEY = "windSpeed"
        private const val DESCRIPTION_KEY = "description"
        private const val POST_IMAGE_KEY = "postImage"
        private const val PARTICIPANTS_KEY = "participants"

        fun fromJSON(json: Map<String, Any>): Post {
            return Post(
                id = json[ID_KEY] as? String ?: "",
                author = json[AUTHOR_KEY] as? String ?: "",
                authorName = json[NAME_KEY] as? String ?: "",
                authorImage = json[IMAGE_KEY] as? String ?: "",
                date = json[DATE_KEY] as? String ?: "",
                sessionDate = json[SESSION_DATE_KEY] as? String ?: "",
                waveHeight = json[WAVE_HEIGHT_KEY] as? String ?: "",
                windSpeed = json[WIND_SPEED_KEY] as? String ?: "",
                description = json[DESCRIPTION_KEY] as? String ?: "",
                postImage = json[POST_IMAGE_KEY] as? String ?: "",
                participants = (json[PARTICIPANTS_KEY] as? List<String>) ?: emptyList()
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
            SESSION_DATE_KEY to sessionDate,
            WAVE_HEIGHT_KEY to waveHeight,
            WIND_SPEED_KEY to windSpeed,
            DESCRIPTION_KEY to description,
            POST_IMAGE_KEY to postImage,
            PARTICIPANTS_KEY to participants
        )
}
