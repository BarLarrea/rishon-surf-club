package com.example.surf_club_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.surf_club_android.model.Model
import com.example.surf_club_android.model.schemas.Post

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadPosts()
    }

    fun loadPosts() {
        _isLoading.value = true
        Model.shared.getUpcomingPosts { posts ->
            _isLoading.postValue(false)
            if (posts.isNotEmpty()) {
                _posts.postValue(posts)
            } else {
                _error.postValue("No posts available")
            }
        }
    }
}