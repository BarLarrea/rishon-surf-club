package com.example.surf_club_android.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.surf_club_android.base.MyApplication
import com.example.surf_club_android.model.Post
import com.example.surf_club_android.model.User


@Database(entities = [Post::class, User::class], version = 3)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
}

object AppLocalDb {

    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "surf-club.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}