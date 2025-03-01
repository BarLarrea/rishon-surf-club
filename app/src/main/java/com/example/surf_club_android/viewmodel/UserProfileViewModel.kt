package com.example.surf_club_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surf_club_android.model.Model
import com.example.surf_club_android.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val model = Model.shared
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUser(userId: String) {
        _isLoading.value = true
        _error.value = null

        model.getUser(userId) { loadedUser ->
            _user.postValue(loadedUser)
            _isLoading.postValue(false)
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    model.signUp(
                        email = updatedUser.email,
                        password = "",
                        firstName = updatedUser.firstName,
                        lastName = updatedUser.lastName,
                        role = updatedUser.role,
                        bitmap = null
                    ) { _, error ->
                        if (error != null) {
                            _error.postValue("User update failed: $error")
                        } else {
                            _user.postValue(updatedUser)
                        }
                        _isLoading.postValue(false)
                    }
                } else {
                    _error.postValue("User not logged in")
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                _error.postValue("Error updating user: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
}
