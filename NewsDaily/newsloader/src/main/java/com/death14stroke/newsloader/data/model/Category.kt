package com.death14stroke.newsloader.data.model

/**
 * Enum for all possible categories of news that can be fetched
 */
enum class Category(val value: String) {
    GENERAL("general"),
    BUSINESS("business"),
    ENTERTAINMENT("entertainment"),
    HEALTH("health"),
    SCIENCE("science"),
    SPORTS("sports"),
    TECHNOLOGY("technology")
}