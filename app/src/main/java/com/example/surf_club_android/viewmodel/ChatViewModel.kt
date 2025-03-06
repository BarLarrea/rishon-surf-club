package com.example.surf_club_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surf_club_android.network.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(private val geminiService: GeminiService) : ViewModel() {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> = _response

    fun sendMessage(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = geminiService.sendMessage(prompt)
                _response.postValue(result) // ✅ הצגת תשובה "נקייה" ולא JSON מלא
            } catch (e: Exception) {
                _response.postValue("Error: ${e.message}")
            }
        }
    }
}


