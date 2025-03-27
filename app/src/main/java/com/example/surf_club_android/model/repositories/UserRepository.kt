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
import com.example.surf_club_android.model.schemas.User
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.Executors

class UserRepository {
    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()
    private val database: AppLocalDbRepository = AppLocalDb.database
    private var roomExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val shared = UserRepository()
    }

    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signIn(email, password, callback)
    }

    fun signUp(email: String, password: String, firstName: String, lastName: String, role: String, bitmap: Bitmap?, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signUp(email, password) { firebaseUser, error ->

            if (firebaseUser != null) {
                if (bitmap != null) {
                    uploadImageToCloudinary(bitmap, firebaseUser.uid,
                        onSuccess = { imageUrl ->
                            Log.d("MODEL", "Image uploaded to Cloudinary: $imageUrl")
                            saveUserToFirebaseAndLocal(firebaseUser, firstName, lastName, email, role, imageUrl, callback)
                        },
                        onError = { errMsg ->
                            Log.e("MODEL", "Image upload to Cloudinary failed: $errMsg")
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

    fun signOut() {
        firebaseModel.signOut()
    }

    fun uploadProfileImage(image: Bitmap, userId: String, callback: (String?) -> Unit) {
        CloudinaryModel().uploadBitmap(image, callback, onError = { callback(null) })
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

    fun updateUser(user: User, callback: (Boolean) -> Unit) {
        FirebaseModel().updateUser(user, callback)
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

    private fun saveUserToFirebaseAndLocal(firebaseUser: FirebaseUser, firstName: String, lastName: String, email: String, role: String, imageUrl: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.saveUser(firebaseUser, firstName, lastName, email, role, imageUrl) { success, saveError ->
            Log.d("DEBUG_PROFILE", "Saving user with profileImageUrl = $imageUrl")

            if (success) {
                roomExecutor.execute {
                    database.userDao().insertUsers(
                        User(
                            id = firebaseUser.uid,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = "",
                            role = role,
                            profileImageUrl = imageUrl,
                            aboutMe = "",
                            sessionIds = emptyList()
                        )
                    )

                    Log.d("MODEL", "Saving user to local: $imageUrl")
                }
                mainHandler.post { callback(firebaseUser, null) }
            } else {
                mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
            }
        }
    }

    fun uploadImageToCloudinary(image: Bitmap, name: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        cloudinaryModel.uploadBitmap(
            bitmap = image,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseModel.getCurrentUser()
    }













}