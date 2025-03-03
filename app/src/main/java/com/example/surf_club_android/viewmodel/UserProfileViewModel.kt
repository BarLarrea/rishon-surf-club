package com.example.surf_club_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.surf_club_android.model.Model
import com.example.surf_club_android.model.Post
import com.example.surf_club_android.model.User


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

    private val _userSessions = MutableLiveData<List<Post>>()
    val userSessions: LiveData<List<Post>> = _userSessions

    fun loadUserSessions(userId: String) {
        model.getUserSessions(userId) { sessions ->
            _userSessions.postValue(sessions)
        }
    }

    fun removeSession(postId: String) {
        _userSessions.value = _userSessions.value?.filterNot { it.id == postId }
    }
}