package com.example.surf_club_android.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
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
                // Count how many posts have a non-empty author field.
                val postsToFetch = posts.count { it.author.isNotEmpty() }
                if (postsToFetch == 0) {
                    // No posts need author info – store and return immediately.
                    roomExecutor.execute {
                        database.postDao().insertPosts(*posts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(posts)
                    }
                } else {
                    // Create a mutable copy to update each post with user info.
                    val updatedPosts = posts.toMutableList()
                    // Use an atomic counter to track the number of pending user fetches.
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

    fun getLastFourPosts(callback: PostsCallback) {
        firebaseModel.getLastFourPosts { posts ->
            Log.d("TAG", "Getting last four posts from Firebase: $posts")
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
                Log.d("TAG", "Getting last four posts from local database")
                roomExecutor.execute {
                    val localPosts = database.postDao().getLastFourPosts()
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    fun addPost(post: Post, profileImage: Bitmap?, callback: EmptyCallback) {
        firebaseModel.addPost(post) { firebaseSuccess ->
            if (!firebaseSuccess) {
                Log.d("TAG", "Firebase add failed")
                mainHandler.post { callback() }
                return@addPost
            }

            Log.d("TAG", "Firebase add succeeded")
            roomExecutor.execute {
                database.postDao().insertPosts(post)
            }

            if (profileImage != null) {
                uploadImageToCloudinary(
                    image = profileImage,
                    name = post.id,
                    onSuccess = { url ->
                        val updatedPost = post.copy(postImage = url)
                        firebaseModel.addPost(updatedPost) { updateSuccess ->
                            if (updateSuccess) {
                                roomExecutor.execute {
                                    database.postDao().insertPosts(updatedPost)
                                }
                            }
                            mainHandler.post { callback() }
                        }
                    },
                    onError = {
                        mainHandler.post { callback() }
                    }
                )
            } else {
                mainHandler.post { callback() }
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

    fun getUser(id: String, callback: (User) -> Unit) {
        firebaseModel.getUser(id) { user ->
            if (user != null) {
                roomExecutor.execute {
                    database.userDao().insertUsers(user)
                }
                mainHandler.post { callback(user) }
            } else {
                roomExecutor.execute {
                    val localUser = database.userDao().getUserById(id)
                        ?: User(id, "Deleted user", "", "", "", "", null)
                    mainHandler.post { callback(localUser) }
                }
            }
        }
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
        // Call FirebaseModel.signUp with the role included.
        firebaseModel.signUp(email, password, firstName, lastName, role) { firebaseUser, error ->
            if (firebaseUser != null) {
                if (bitmap != null) {
                    uploadImageToCloudinary(bitmap, firebaseUser.uid, onSuccess = { imageUrl ->
                        Log.d("TAG", "Image uploaded to Cloudinary: $imageUrl")
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
                    }, onError = { errMsg ->
                        Log.e("TAG", "Image upload to Cloudinary failed: $errMsg")
                        firebaseModel.saveUser(firebaseUser, firstName, lastName, email, role, "") { success, saveError ->
                            if (success) {
                                roomExecutor.execute {
                                    database.userDao().insertUsers(
                                        User(firebaseUser.uid, firstName, lastName, email, "", role, "")
                                    )
                                }
                                mainHandler.post { callback(firebaseUser, null) }
                            } else {
                                mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
                            }
                        }
                    })
                } else {
                    firebaseModel.saveUser(firebaseUser, firstName, lastName, email, role, "") { success, saveError ->
                        if (success) {
                            roomExecutor.execute {
                                database.userDao().insertUsers(
                                    User(firebaseUser.uid, firstName, lastName, email, "", role, "")
                                )
                            }
                            mainHandler.post { callback(firebaseUser, null) }
                        } else {
                            mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
                        }
                    }
                }
            } else {
                mainHandler.post { callback(null, error ?: "Sign up failed") }
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

    private fun uploadImageToFirebase(image: Bitmap, name: String, callback: (String?) -> Unit) {
        firebaseModel.uploadImage(image, name, callback)
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
}
