package com.example.surf_club_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.surf_club_android.network.GeminiService

class ChatViewModelFactory(private val geminiService: GeminiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(geminiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
