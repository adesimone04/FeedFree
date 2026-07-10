package com.example.feedfree.models

data class User(
    val id: String,
    val name: String,
    val username: String,
    val points: Int,
    val level: Int = 1,
    val avatarUrl: String? = null,
    val badges: List<Badges> = emptyList()
)