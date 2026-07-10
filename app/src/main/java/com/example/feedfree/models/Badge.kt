package com.example.feedfree.models

data class Badge (
    val id: String,
    val name: String,
    val type: Tier,
    val percentage: Int
)

enum class Tier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}
