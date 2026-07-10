package com.example.feedfree.data

import com.example.feedfree.models.*
import kotlinx.coroutines.delay

object MockRepository {

    suspend fun getCurrentUser(): User {
        delay(800)

        val mockBadges = listOf(
            Badges(
                id = 1,
                name = "Pioniere",
                badgesUrl = "https://ui-avatars.com/api/?name=P&background=FFD700&color=fff"
            ),
            Badges(
                id = 2,
                name = "Top 10",
                badgesUrl = "https://ui-avatars.com/api/?name=10&background=C0C0C0&color=fff"
            )
        )

        return User(
            id = "usr_001",
            name = "Mario Rossi",
            username = "@marior",
            points = 2000,
            level = 15,
            avatarUrl = "https://ui-avatars.com/api/?name=Mario+Rossi",
            badges = mockBadges
        )
    }

    suspend fun getLeaderboard(): List<User> {
        delay(1000)
        return listOf(
            getCurrentUser(),
            User("usr_002", "Antonio", "@antonio", 3500, 25),
            User("usr_003", "Giulia Neri", "@giulian", 2100, 18),
            User("usr_004", "Yugi Tronchese", "@yugi", 1000, 8)
        ).sortedByDescending { it.points }
    }

    suspend fun getDailyScreenTime(): DailyScreenTime {
        delay(500)
        // 255 minuti = 4h 15m; 480 minuti = 8h
        return DailyScreenTime(totalSpentMinutes = 255, dailyGoalMinutes = 480)
    }

    suspend fun getAppList(): List<AppItem> {
        delay(600)
        return listOf(
            AppItem(
                id = "app_1",
                name = "Instagram",
                state = AppState.MONITORED,
                timeSpentMinutes = 90 // 1h e 30m
            ),
            AppItem(
                id = "app_2",
                name = "Youtube",
                state = AppState.MONITORED,
                timeSpentMinutes = 20
            ),
            AppItem(
                id = "app_3",
                name = "LinkedIn",
                state = AppState.TIMER,
                timeSpentMinutes = 15,
                timerLimitMinutes = 30 // 15m / 30m
            ),
            AppItem(
                id = "app_4",
                name = "TikTok",
                state = AppState.BLOCKED
            ),
            AppItem(
                id = "app_5",
                name = "Microsoft Teams",
                state = AppState.DEFAULT,
                timeSpentMinutes = 130 // 2h 10m
            )
        )
    }

    suspend fun getCustomActivities(): List<CustomActivity> {
        delay(700)
        return listOf(
            CustomActivity(
                id = "act_1",
                name = "Leggere Harry Potter 1",
                description = "Relax serale",
                isCompleted = true,
                goals = listOf(Goal("g1", "Leggi 20 pagine di un libro", true))
            ),
            CustomActivity(
                id = "act_2",
                name = "Studiare ETC slide 12",
                description = "Preparazione esame",
                isCompleted = true,
                goals = emptyList()
            ),
            CustomActivity(
                id = "act_3",
                name = "Finire progetto IUM",
                description = "Sviluppo prototipo Figma",
                isCompleted = true,
                goals = listOf(Goal("g2", "Ottieni tutti i Badges", false))
            )
        )
    }
}