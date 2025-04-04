package com.example.surf_club_android.model.network

import android.util.Log
import com.example.surf_club_android.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiService {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val apiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=$apiKey"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun sendMessage(userPrompt: String, messageHistory: List<String>): String {
        return withContext(Dispatchers.IO) {
            try {
                val messages = mutableListOf<Map<String, Any>>()
                messageHistory.forEach { message ->
                    val role = if (message.startsWith("User:")) "user" else "model"
                    val text = message.substringAfter(": ")
                    messages.add(mapOf("parts" to listOf(mapOf("text" to text)), "role" to role))
                }
                messages.add(mapOf("parts" to listOf(mapOf("text" to userPrompt)), "role" to "user"))

                val requestBody = """
                    {
                        "contents": ${gson.toJson(messages)}
                    }
                """.trimIndent()

                Log.d("GeminiService", "API Request: $requestBody")

                val request = Request.Builder()
                    .url(apiUrl)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("API Error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response")

                Log.d("GeminiService", "API Response: $responseBody")

                val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
                val textResponse = jsonResponse["candidates"]
                    ?.asJsonArray?.get(0)?.asJsonObject
                    ?.get("content")?.asJsonObject
                    ?.get("parts")?.asJsonArray?.get(0)?.asJsonObject
                    ?.get("text")?.asString ?: "Error: Could not parse response"

                return@withContext textResponse
            } catch (e: Exception) {
                Log.e("GeminiService", "Request failed", e)
                return@withContext "Error: Request failed: ${e.message}"
            }
        }
    }
}