package com.example.feedfree.data

import com.example.feedfree.models.User
import kotlinx.coroutines.delay

object MockRepository {

    // Simula una chiamata di rete con 'suspend' e un leggero ritardo
    suspend fun getCurrentUser(): User {
        delay(800) // Simula 800ms di caricamento
        return User(
            id = "usr_001",
            name = "Mario Rossi",
            username = "@marior",
            points = 2000,
            avatarUrl = "https://ui-avatars.com/api/?name=Mario+Rossi"
        )
    }

    // Aggiunta utile per mockare classifiche o feed
    suspend fun getLeaderboard(): List<User> {
        delay(1000)
        return listOf(
            getCurrentUser(),
            User("usr_002", "Luigi Verdi", "@luigiv", 1850),
            User("usr_003", "Giulia Neri", "@giulian", 2100),
            User("usr_004", "Yugi Tronchese", "@yugi", 1000)
        ).sortedByDescending { it.points }
    }
}