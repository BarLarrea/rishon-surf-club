package com.example.surf_club_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.surf_club_android.model.Model
import com.example.surf_club_android.model.User
import com.google.firebase.auth.FirebaseAuth

class EditUserProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _saveSuccess = MutableLiveData(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun loadUserData() {
        _isLoading.value = true
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            Model.shared.getUser(currentUserId) { user ->
                if (user != null) {
                    _user.postValue(user)
                } else {
                    _error.postValue("Failed to load user data")
                }
                _isLoading.postValue(false)
            }
        } else {
            _error.postValue("No user logged in")
            _isLoading.postValue(false)
        }
    }

    fun updateUserProfile(updatedUser: User) {
        _isLoading.value = true
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Using the public method from Model instead of the private one
            Model.shared.updateUser(updatedUser) { success ->
                if (success) {
                    _saveSuccess.postValue(true)
                } else {
                    _error.postValue("Failed to update profile")
                }
                _isLoading.postValue(false)
            }
        } else {
            _error.postValue("No user logged in")
            _isLoading.postValue(false)
        }
    }
}