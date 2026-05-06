package com.citypulse.app.domain.model

data class Favorite(
    val place: Place,
    val note: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    val placeId: String get() = place.id
    val hasNote: Boolean get() = note.isNotBlank()
}