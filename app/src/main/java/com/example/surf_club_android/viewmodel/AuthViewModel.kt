package com.example.surf_club_android.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surf_club_android.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        auth.currentUser?.let { firebaseUser ->
            getUserData(firebaseUser.uid)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    getUserData(firebaseUser.uid)
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error during sign in.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun signUp(firstName: String, lastName: String, email: String, password: String, role: String, profileImageUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                Log.d("TAG", "${result}")
                result.user?.let { firebaseUser ->
                    var profileImageUrl = ""
                    profileImageUri?.let { uri ->
                        profileImageUrl = uploadImageToCloudinary(uri)
                    }
                    val newUser = User(
                        id = firebaseUser.uid,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = "", // Don't store the password
                        role = role,
                        profileImageUrl = profileImageUrl
                    )
                    saveUserToFirestore(newUser)
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error during sign up.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.postValue(null)
    }

    private fun getUserData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                if (document.exists()) {
                    val userData = document.data
                    userData?.let {
                        val user = User.fromJSON(it)
                        _user.postValue(user)
                    }
                } else {
                    _error.postValue("User data not found.")
                }
            } catch (e: Exception) {
                _error.postValue("Failed to retrieve user data.")
            }
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection("users").document(user.id).set(user.json).await()
            _user.postValue(user)
        } catch (e: Exception) {
            _error.postValue("Failed to save user data.")
        }
    }

    private suspend fun uploadImageToCloudinary(imageUri: Uri): String {
        return suspendCoroutine { continuation ->
            MediaManager.get().upload(imageUri).callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Optionally handle start event or leave empty.
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["url"] as? String ?: ""
                    continuation.resume(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Optionally update progress.
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
            }).dispatch()
        }
    }
}