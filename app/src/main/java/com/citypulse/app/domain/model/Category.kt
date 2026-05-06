package com.citypulse.app.domain.model

enum class Category(val label: String, val icon: String) {
    RESTAURANT("Restaurant", "🍽 "),
    PARK("Parc", "🌳"),
    MUSEUM("Musée", "🏛 "),
    SHOP("Commerce", "🛍 "),
    CAFE("Café", "☕"),
    HOTEL("Hôtel", "🏨"),
    TRANSPORT("Transport", "🚇"),
    OTHER("Autre", "📍");

    companion object {
        fun fromString(value: String): Category =
            entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: OTHER
    }
}