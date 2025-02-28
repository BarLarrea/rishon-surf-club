package com.example.surf_club_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surf_club_android.model.User
import com.example.surf_club_android.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loadedUser = repository.getUserById(userId)
                _user.value = loadedUser
                if (loadedUser == null) {
                    _error.value = "User not found"
                }
            } catch (e: Exception) {
                _error.value = "Error loading user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val success = repository.updateUser(firebaseUser, updatedUser)
                    if (success) {
                        _user.value = updatedUser
                    } else {
                        _error.value = "User update failed"
                    }
                } else {
                    _error.value = "User not logged in"
                }
            } catch (e: Exception) {
                _error.value = "Error updating user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}