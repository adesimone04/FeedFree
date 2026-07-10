package com.example.feedfree.models

data class User(
    val id: String, // Identificatore univoco essenziale
    val name: String,
    val username: String,
    val points: Int,
    val badges: List<Badge>,
    val avatarUrl: String? = null // Opzionale, utile per la UI
)