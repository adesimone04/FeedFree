package com.example.feedfree.models

data class User(
    val id: String,
    val name: String,
    val username: String,
    val points: Int,
    val avatarUrl: String? = null,
    val badges: List<Badges> = emptyList()
)