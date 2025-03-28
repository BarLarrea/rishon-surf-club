package com.example.surf_club_android.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.surf_club_android.model.schemas.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>
    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: String): User
    @Update
    fun updateUser(user: User)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: User)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg users: User)
    @Delete
    fun delete(user: User)
}
