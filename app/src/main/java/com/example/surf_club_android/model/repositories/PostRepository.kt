package com.example.surf_club_android.model.repositories

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.surf_club_android.model.dao.AppLocalDb
import com.example.surf_club_android.model.dao.AppLocalDbRepository
import com.example.surf_club_android.model.network.CloudinaryModel
import com.example.surf_club_android.model.network.FirebaseModel
import com.example.surf_club_android.model.schemas.Post
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class PostRepository {
    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()
    private val database: AppLocalDbRepository = AppLocalDb.database
    private var roomExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val shared = PostRepository()
    }

    fun getUpcomingPosts(callback: (List<Post>) -> Unit) {
        firebaseModel.getAllPosts { posts ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val upcomingPosts = posts.mapNotNull { post ->
                try {
                    val sessionDate = dateFormat.parse(post.sessionDate.trim())
                    if (sessionDate != null && !sessionDate.before(today)) {
                        post
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { post ->
                try {
                    dateFormat.parse(post.sessionDate.trim())
                } catch (e: Exception) {
                    null
                }
            }

            callback(upcomingPosts)
        }
    }

    fun addPost(post: Post, profileImage: Bitmap?, callback: (Boolean, String?) -> Unit) {
        if (profileImage != null) {
            uploadImageToCloudinary(
                image = profileImage,
                name = post.id,
                onSuccess = { url ->
                    val updatedPost = post.copy(postImage = url)
                    savePostToFirebaseAndLocal(updatedPost, callback)
                },
                onError = { error ->
                    Log.e("Model", "Error uploading image to Cloudinary: $error")
                    callback(false, null)
                }
            )
        } else {
            savePostToFirebaseAndLocal(post, callback)
        }
    }

    fun getPostById(postId: String, callback: (Post?) -> Unit) {
        firebaseModel.getPostById(postId, callback)
    }

    fun toggleSessionParticipation(userId: String, postId: String, callback: (Boolean, Boolean) -> Unit) {
        firebaseModel.getPostById(postId) { post ->
            if (post != null) {
                val updatedParticipants = post.participants.toMutableList()
                val isJoining = !updatedParticipants.contains(userId)

                if (isJoining) {
                    updatedParticipants.add(userId)
                } else {
                    updatedParticipants.remove(userId)
                }

                val updatedPost = post.copy(participants = updatedParticipants)

                firebaseModel.getUser(userId) { user ->
                    if (user != null) {
                        val updatedSessions = user.sessionIds.toMutableList()

                        if (isJoining) {
                            updatedSessions.add(postId)
                        } else {
                            updatedSessions.remove(postId)
                        }

                        val updatedUser = user.copy(sessionIds = updatedSessions)

                        firebaseModel.updatePost(updatedPost) { postUpdated ->
                            if (postUpdated) {
                                firebaseModel.updateUser(updatedUser) { userUpdated ->
                                    if (userUpdated) {
                                        roomExecutor.execute {
                                            database.postDao().insertPosts(updatedPost)
                                            database.userDao().insertUsers(updatedUser)
                                        }
                                        callback(true, isJoining)
                                    } else {
                                        callback(false, isJoining)
                                    }
                                }
                            } else {
                                callback(false, isJoining)
                            }
                        }
                    } else {
                        callback(false, isJoining)
                    }
                }
            } else {
                callback(false, false)
            }
        }
    }

    fun updatePost(post: Post, newImage: Bitmap?, callback: (Boolean) -> Unit) {
        if (newImage != null) {
            // Upload new image first.
            uploadImageToCloudinary(newImage, post.id, onSuccess = { imageUrl ->
                val updatedPost = post.copy(postImage = imageUrl)
                firebaseModel.updatePost(updatedPost) { success ->
                    if (success) {
                        roomExecutor.execute {
                            database.postDao().insertPosts(updatedPost)
                            mainHandler.post { callback(true) }
                        }
                    } else {
                        mainHandler.post { callback(false) }
                    }
                }
            }, onError = {
                mainHandler.post { callback(false) }
            })
        } else {
            // No new image: update the post directly.
            firebaseModel.updatePost(post) { success ->
                if (success) {
                    roomExecutor.execute {
                        database.postDao().insertPosts(post)
                        mainHandler.post { callback(true) }
                    }
                } else {
                    mainHandler.post { callback(false) }
                }
            }
        }
    }

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        firebaseModel.deletePost(postId) {
            // For simplicity, we assume deletion was successful if callback is invoked.
            callback(true)
        }
    }

    private fun savePostToFirebaseAndLocal(post: Post, callback: (Boolean, String?) -> Unit) {
        firebaseModel.addPost(post) { firebaseSuccess ->
            if (firebaseSuccess) {
                roomExecutor.execute {
                    database.postDao().insertPosts(post)
                    mainHandler.post {
                        callback(true, post.postImage)
                    }
                }
            } else {
                Log.d("TAG", "Firebase add failed")
                callback(false, null)
            }
        }
    }

    private fun uploadImageToCloudinary(image: Bitmap, name: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        cloudinaryModel.uploadBitmap(
            bitmap = image,
            onSuccess = onSuccess,
            onError = onError
        )
    }


}