package com.example.surf_club_android.model

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.surf_club_android.base.EmptyCallback
import com.example.surf_club_android.base.PostsCallback
import com.example.surf_club_android.base.UsersCallback
import com.example.surf_club_android.model.dao.AppLocalDb
import com.example.surf_club_android.model.dao.AppLocalDbRepository
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class Model private constructor() {
    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()
    private val database: AppLocalDbRepository = AppLocalDb.database
    private var roomExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val shared = Model()
    }

    fun getAllPosts(callback: PostsCallback) {
        firebaseModel.getAllPosts { posts ->
            Log.d("TAG", "Getting posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                val postsToFetch = posts.count { it.author.isNotEmpty() }
                if (postsToFetch == 0) {
                    roomExecutor.execute {
                        database.postDao().insertPosts(*posts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(posts)
                    }
                } else {
                    val updatedPosts = posts.toMutableList()
                    val counter = AtomicInteger(postsToFetch)
                    for ((index, post) in updatedPosts.withIndex()) {
                        if (post.author.isNotEmpty()) {
                            Log.d("TAG", "Fetching user for post: ${post.id}")
                            getUser(post.author) { user ->
                                updatedPosts[index] = post.copy(
                                    authorName = user?.firstName ?: "",
                                    authorImage = user?.profileImageUrl ?: ""
                                )
                                if (counter.decrementAndGet() == 0) {
                                    roomExecutor.execute {
                                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                                    }
                                    mainHandler.post {
                                        callback(updatedPosts)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("TAG", "Getting posts from local database")
                roomExecutor.execute {
                    val localPosts = database.postDao().getAllPosts()
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
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

    fun getAllUserPosts(id: String, callback: PostsCallback) {
        firebaseModel.getAllUserPosts(id) { posts ->
            Log.d("TAG", "Getting user posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                getUser(id) { user ->
                    val updatedPosts = posts.map { post ->
                        post.copy(
                            authorName = user?.firstName ?: "",
                            authorImage = user?.profileImageUrl ?: ""
                        )
                    }
                    roomExecutor.execute {
                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(updatedPosts)
                    }
                }
            } else {
                Log.d("TAG", "Getting user posts from local database")
                roomExecutor.execute {
                    val localPosts = database.postDao().getPostsByAuthor(id)
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    fun getUserSessions(userId: String, callback: (List<Post>) -> Unit) {
        firebaseModel.getUserSessions(userId) { posts ->
            if (posts.isNotEmpty()) {
                roomExecutor.execute {
                    database.postDao().insertPosts(*posts.toTypedArray())
                }
            }
            mainHandler.post {
                callback(posts)
            }
        }
    }

    fun getUser(id: String, callback: (User?) -> Unit) {
        firebaseModel.getUser(id) { user ->
            if (user != null) {
                roomExecutor.execute {
                    database.userDao().insertUsers(user)
                }
                mainHandler.post {
                    callback(user)
                }
            } else {
                roomExecutor.execute {
                    val localUser = database.userDao().getUserById(id)
                    mainHandler.post { callback(localUser) }
                }
            }
        }
    }

    fun getPostById(postId: String, callback: (Post?) -> Unit) {
        firebaseModel.getPostById(postId, callback)
    }

    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signIn(email, password, callback)
    }

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String,
        bitmap: Bitmap?,
        callback: (FirebaseUser?, String?) -> Unit
    ) {
        firebaseModel.signUp(email, password) { firebaseUser, error ->
            if (firebaseUser != null) {
                if (bitmap != null) {
                    uploadImageToCloudinary(bitmap, firebaseUser.uid,
                        onSuccess = { imageUrl ->
                            Log.d("TAG", "Image uploaded to Cloudinary: $imageUrl")
                            saveUserToFirebaseAndLocal(firebaseUser, firstName, lastName, email, role, imageUrl, callback)
                        },
                        onError = { errMsg ->
                            Log.e("TAG", "Image upload to Cloudinary failed: $errMsg")
                            saveUserToFirebaseAndLocal(firebaseUser, firstName, lastName, email, role, "", callback)
                        }
                    )
                } else {
                    saveUserToFirebaseAndLocal(firebaseUser, firstName, lastName, email, role, "", callback)
                }
            } else {
                mainHandler.post { callback(null, error ?: "Sign up failed") }
            }
        }
    }

    private fun saveUserToFirebaseAndLocal(
        firebaseUser: FirebaseUser,
        firstName: String,
        lastName: String,
        email: String,
        role: String,
        imageUrl: String,
        callback: (FirebaseUser?, String?) -> Unit
    ) {
        firebaseModel.saveUser(firebaseUser, firstName, lastName, email, role, imageUrl) { success, saveError ->
            if (success) {
                roomExecutor.execute {
                    database.userDao().insertUsers(
                        User(firebaseUser.uid, firstName, lastName, email, "", role, imageUrl)
                    )
                }
                mainHandler.post { callback(firebaseUser, null) }
            } else {
                mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
            }
        }
    }

    fun getAllUsers(callback: UsersCallback) {
        firebaseModel.getAllUsers { users ->
            if (users.isNotEmpty()) {
                roomExecutor.execute {
                    database.userDao().insertUsers(*users.toTypedArray())
                }
                mainHandler.post {
                    callback(users)
                }
            } else {
                roomExecutor.execute {
                    val localUsers = database.userDao().getAll()
                    mainHandler.post {
                        callback(localUsers)
                    }
                }
            }
        }
    }

    fun signOut() {
        firebaseModel.signOut()
    }

    fun uploadProfileImage(image: Bitmap, userId: String, callback: (String?) -> Unit) {
        CloudinaryModel().uploadBitmap(image, callback, onError = { callback(null) })
    }

    fun updateUser(user: User, callback: (Boolean) -> Unit) {
        FirebaseModel().updateUser(user, callback)
    }

    private fun uploadImageToCloudinary(
        image: Bitmap,
        name: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        cloudinaryModel.uploadBitmap(
            bitmap = image,
            onSuccess = onSuccess,
            onError = onError
        )
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
            }, onError = { error ->
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
    fun getCurrentUserRole(currentUserId: String, callback: (String?) -> Unit) {
        // This reuses your existing getUser function.
        getUser(currentUserId) { user ->
            callback(user?.role)
        }
    }





}