package com.example.feedfree.models

import android.R

// Enum per lo stato delle applicazioni (Monitorata, Con Timer, Bloccata)
enum class AppState { MONITORED, TIMER, BLOCKED, DEFAULT }

data class AppItem(
    val id: String,
    val name: String,
    val state: AppState,
    val timeSpentMinutes: Int = 0,     // Es: 90 per 1h e 30m
    val timerLimitMinutes: Int? = null // Es: 30 se l'utente ha impostato un limite
)

data class DailyScreenTime(
    val totalSpentMinutes: Int,       // Es: 255 (4h 15m)
    val dailyGoalMinutes: Int         // Es: 480 (8h)
)

data class Goal(
    val id: String,
    val name: String,
    val isCompleted: Boolean
)

data class CustomActivity(
    val id: String,
    val name: String,
    val description: String?,
    val iconId: Int? = null,
    val isCompleted: Boolean,
    val goals: List<Goal> = emptyList(),
    val tier: Tier = Tier.BRONZE
)