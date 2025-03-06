package com.example.surf_club_android.network

data class ChatRequest(
    val model: String = "gemini-pro",
    val prompt: String
)

data class ChatResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: String
)
