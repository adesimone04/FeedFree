package com.example.feedfree.data

import com.example.feedfree.models.Badge
import com.example.feedfree.models.Tier
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
            avatarUrl = "https://ui-avatars.com/api/?name=Mario+Rossi",
            badges = listOf(
                Badge(
                    id = "000A", name = "leggi 20 libri", type = Tier.PLATINUM, percentage = 100
                ),
                Badge(
                    id = "000B", name = "fai una passeggiata", type = Tier.BRONZE, percentage = 100
                ),
            )
        )
    }

    // Aggiunta utile per mockare classifiche o feed
    suspend fun getLeaderboard(): List<User> {
        delay(1000)
        return listOf(
            getCurrentUser(),
            User("usr_002", "Luigi Verdi", "@luigiv", 1850, badges = listOf(
                Badge(
                    id = "000C", name = "leggi 20 libri", type = Tier.PLATINUM, percentage = 100
                ),
            )),

            User("usr_003", "Giulia Neri", "@giulian", 2100, badges = listOf(
                Badge(
                    id = "000D", name = "leggi 20 libri", type = Tier.PLATINUM, percentage = 100
                ),
            )),

            User("usr_004", "Yugi Tronchese", "@yugi", 1000, badges = listOf(
                Badge(
                    id = "000E", name = "leggi 20 libri", type = Tier.PLATINUM, percentage = 100
                ),
            ))

        ).sortedByDescending { it.points }
    }
}