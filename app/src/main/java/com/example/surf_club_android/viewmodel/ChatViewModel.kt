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
    private val messageHistory = mutableListOf<String>()  // List to store conversation history
    private val systemPrompt = """
        Your name is Kelly, a friendly and knowledgeable assistant who specializes in mental coaching, surfing expertise,
         and promoting a healthy lifestyle. You are an expert in surfing techniques, wave forecasts (especially in Israel),
         and maintaining physical and mental well-being.
         Provide clear, concise, and actionable answers to user questions.
         Avoid asking unnecessary follow-up questions unless essential for understanding the user's request.
         Use appropriate emojis sparingly to make your responses more engaging, friendly, and motivational.
         For example, use 🌊 for surfing, 🧘‍♂️ for mental health, 🍎 for healthy eating, and 💪 for fitness.
         If a query is unrelated to your expertise, politely redirect the conversation to topics like surfing, mental health,
         or a healthy lifestyle. Always maintain an encouraging and motivational tone.
         The user is not Kelly, only the assistant is named Kelly which is you, remember it for the whole session.
         """.trimIndent()

    // Send only the system prompt at the start of the conversation
    fun sendInitialMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = geminiService.sendMessage(systemPrompt)
                messageHistory.add(result)  // Save response in history
                _response.postValue(result)
            } catch (e: Exception) {
                _response.postValue("Error: ${e.message}")
            }
        }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = geminiService.sendMessage(userMessage)
                _response.postValue(result)
            } catch (e: Exception) {
                _response.postValue("Error: ${e.message}")
            }
        }
    }
}
