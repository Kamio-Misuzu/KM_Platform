package com.example.mforum.data

data class TeachingRequest(
    val userId: Int,
    val topic: String? = null,
    val difficulty: String = "beginner"
)
