package com.example.feedfree.data

import com.example.feedfree.models.*
import kotlinx.coroutines.delay

object MockRepository {

    suspend fun getCurrentUser(): User {
        delay(800)

        // Creazione dinamica dei trofei
        val platBadges = listOf(Badges(1, "Completista Suprema", Tier.PLATINUM, "https://ui-avatars.com/api/?name=P"))
        val goldBadges = List(5) { Badges(10 + it, "Traguardo Oro ${it + 1}", Tier.GOLD, "https://ui-avatars.com/api/?name=G") }
        val silverBadges = List(2) { Badges(20 + it, "Traguardo Argento ${it + 1}", Tier.SILVER, "https://ui-avatars.com/api/?name=S") }
        val bronzeBadges = List(12) { Badges(30 + it, "Traguardo Bronzo ${it + 1}", Tier.BRONZE, "https://ui-avatars.com/api/?name=B") }

        val mockBadges = platBadges + goldBadges + silverBadges + bronzeBadges

        return User(
            id = "usr_001",
            name = "Mario Rossi",
            username = "OiramR",
            points = 2000,
            level = 15,
            avatarUrl = "https://ui-avatars.com/api/?name=A.+Cimmino",
            badges = mockBadges,
            friends = listOf(
                User("usr_002", "Antonio", "@antonio", 3500, 25, null, emptyList(), null),
                User("usr_003", "Serena", "@aneres", 2000, 15, null, emptyList(), null),
                User("usr_004", "Matteo", "@mato", 1500, 12, null, emptyList(), null)
            )
        )
    }

    suspend fun getCustomActivities(): List<CustomActivity> {
        delay(700)
        return listOf(
            // ATTIVITÀ PENDING (In corso)
            CustomActivity(
                id = "act_1",
                name = "Leggere Harry Potter 1",
                description = "Relax serale",
                isCompleted = false,
                goals = listOf(
                    Goal("g1", "Leggi capitolo 1", true),
                    Goal("g2", "Leggi capitolo 2", false),
                    Goal("g3", "Leggi capitolo 3", false)
                ),
                tier = Tier.SILVER
            ),
            CustomActivity(
                id = "act_2",
                name = "Studiare ETC slide 12",
                description = "Preparazione esame",
                isCompleted = false,
                goals = listOf(
                    Goal("g4", "Finire slide 5", true),
                    Goal("g5", "Finire slide 10", true),
                    Goal("g6", "Ripasso generale", false)
                ),
                tier = Tier.GOLD
            ),
            CustomActivity(
                id = "act_3",
                name = "Iniziare progetto IUM",
                description = "Sviluppo UI",
                isCompleted = false,
                goals = listOf(
                    Goal("g7", "Creare Wireframe", false),
                    Goal("g8", "Disegnare UI", false)
                ),
                tier = Tier.PLATINUM
            ),

            // ATTIVITÀ COMPLETATE (Da bacheca)
            CustomActivity(
                id = "act_4",
                name = "Attività offline",
                description = "Disconnessione totale",
                isCompleted = true,
                goals = listOf(
                    Goal("g9", "Spegni il telefono per 2 ore", true)
                ),
                tier = Tier.BRONZE
            ),
            CustomActivity(
                id = "act_5",
                name = "Corso primo soccorso",
                description = "Formazione personale",
                isCompleted = true,
                goals = listOf(
                    Goal("g10", "Teoria", true),
                    Goal("g11", "Pratica", true),
                    Goal("g12", "Test finale", true)
                ),
                tier = Tier.GOLD
            ),
            CustomActivity(
                id = "act_6",
                name = "Workshop Figma",
                description = "Design system",
                isCompleted = true,
                goals = listOf(
                    Goal("g13", "Creare componenti", true),
                    Goal("g14", "Collegare prototipo", true)
                ),
                tier = Tier.PLATINUM
            )
        )
    }
}