package com.example.mforum.data

data class TeachingProgressResponse(
    val sessionId: String,
    val progress: Int,
    val completed: Boolean,
    val nextStep: String?
)