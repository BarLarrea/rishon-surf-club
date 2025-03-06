package com.example.surf_club_android.network

import android.util.Log
import com.example.surf_club_android.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiService {
    private val apiKey = BuildConfig.GEMINI_API_KEY // ✅ שימוש במפתח API
    private val apiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=$apiKey"
    private val httpClient = OkHttpClient()
    private val gson = Gson()

    suspend fun sendMessage(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = """
                    {
                        "contents": [{"parts": [{"text": "$prompt"}]}]
                    }
                """.trimIndent()

                val request = Request.Builder()
                    .url(apiUrl)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("API Error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response")

                // ✅ חילוץ התשובה מה-JSON
                val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
                val textResponse = jsonResponse["candidates"]
                    ?.asJsonArray?.get(0)?.asJsonObject
                    ?.get("content")?.asJsonObject
                    ?.get("parts")?.asJsonArray?.get(0)?.asJsonObject
                    ?.get("text")?.asString ?: "Error: Could not parse response"

                return@withContext textResponse
            } catch (e: Exception) {
                throw Exception("Request failed: ${e.message}")
            }
        }
    }


    suspend fun chat(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // System prompt - guiding Gemini to act as a surfing and mental coach
                val systemPrompt = """
                    You are a **surfing and mental preparation coach** who helps people, 
                    including those with special needs, get ready for their surfing sessions.
                    Be friendly, encouraging, and offer both **technical surfing tips** and **mental resilience advice**.
                """.trimIndent()
                val jsonBody = """{
                    "contents": [
                        {"parts": [{"text": "$systemPrompt"}]},
                        {"parts": [{"text": "$userMessage"}]}
                    ]
                }""".trimIndent()

                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                Log.d("GeminiService", "Sending request to Gemini API...")

                val response = httpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("GeminiService", "API Error: ${response.code}")
                    throw Exception("API Error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: "Error: Empty response"
                Log.d("GeminiService", "API Response: $responseBody")
                return@withContext responseBody
            } catch (e: Exception) {
                throw Exception("Request failed: ${e.message}")
            }
        }
    }
}
