package com.example.surf_club_android.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surf_club_android.model.network.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.surf_club_android.model.repositories.UserRepository

class ChatViewModel(private val geminiService: GeminiService) : ViewModel() {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> = _response
    private val messageHistory = mutableListOf<String>()  // List to store conversation history
    private val systemPrompt = """
        Your name is Kelly, a friendly and knowledgeable assistant who specializes in mental coaching, surfing expertise,
        and promoting a healthy lifestyle. You are an expert in surfing techniques, wave forecasts (especially in Israel),
        and maintaining physical and mental well-being.
        Respond to user messages as if you are already in an ongoing conversation.
        Avoid asking unnecessary follow-up questions unless essential for understanding the user's request.
        Use appropriate emojis sparingly to make your responses more engaging, friendly, and motivational.
        For example, use  for surfing, ‍♂️ for mental health,  for healthy eating, and  for fitness.
        If a query is unrelated to your expertise, politely redirect the conversation to topics like surfing, mental health,
        or a healthy lifestyle. Always maintain an encouraging and motivational tone.
        The user is not Kelly, only the assistant is named Kelly, which is you. Remember this for the entire session.
        Use the conversation history provided to maintain context throughout the conversation.
        Conversation History:
        """.trimIndent()

    fun sendInitialMessage() {
       val firstMessage = "I'm Kelly, your surfing, mental health, and lifestyle assistant. Ready to catch some waves? ☀️‍♂️‍♀️"
       val userId = UserRepository.shared.getCurrentUser()?.uid

       UserRepository.shared.getUser(userId ?: "") { user ->
           val userName = user?.firstName ?: ""
           Log.d("chat", "user name is: $userName")
           _response.postValue("Hi $userName, $firstMessage")
           if (userId == null) {
               Log.d("chat", "User ID is null")
           }
       }

        // Send the system prompt to Gemini without handling the response
        viewModelScope.launch(Dispatchers.IO) {
            try {
                geminiService.sendMessage(systemPrompt, messageHistory)
            } catch (e: Exception) {
                Log.e("chat", "Error: ${e.message}")
                _response.postValue("Sorry, I'm having trouble connecting right now. Please try again later.")
            }
        }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageHistory.add("User: $userMessage")
                val conversation = buildConversation()
                val result = geminiService.sendMessage(conversation, messageHistory)
                messageHistory.add("Kelly: $result")
                _response.postValue(result)
            } catch (e: Exception) {
                Log.e("chat", "API Error: ${e.message}", e)
                _response.postValue("Sorry, something went wrong. Please try again later.")
            }
        }
    }

    private fun buildConversation(): String {
        val conversationBuilder = StringBuilder(systemPrompt)
        messageHistory.forEach {
            conversationBuilder.append("\n$it")
        }
        return conversationBuilder.toString()
    }
}

