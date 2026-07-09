package com.example.feedfree.data

import com.example.feedfree.models.Badges
import com.example.feedfree.models.User
import kotlinx.coroutines.delay

object MockRepository {

    suspend fun getCurrentUser(): User {
        delay(800)

        val mockBadges = listOf(
            Badges(
                id = 1,
                name = "Pioniere",
                badgesUrl = "https://ui-avatars.com/api/?name=P&background=FFD700&color=fff&rounded=true"
            ),
            Badges(
                id = 2,
                name = "Top 10",
                badgesUrl = "https://ui-avatars.com/api/?name=10&background=C0C0C0&color=fff&rounded=true"
            ),
            Badges(
                id = 3,
                name = "Scrittore",
                badgesUrl = "https://ui-avatars.com/api/?name=S&background=0D8ABC&color=fff&rounded=true"
            )
        )

        return User(
            id = "usr_001",
            name = "Mario Rossi",
            username = "@marior",
            points = 2000,
            avatarUrl = "https://ui-avatars.com/api/?name=Mario+Rossi",
            badges = mockBadges
        )
    }

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