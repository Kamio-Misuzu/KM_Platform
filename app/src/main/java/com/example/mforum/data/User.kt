package com.example.mforum.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val avatarUrl: String = "",
    val avatarType: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    var lastLogin: Long = System.currentTimeMillis()
)