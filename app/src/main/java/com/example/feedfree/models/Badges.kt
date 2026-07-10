package com.example.feedfree.models

data class Badges(
    val id: Int,
    val name: String,
    val type: Tier,
    val badgesUrl: String? = null
)

enum class Tier{
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}