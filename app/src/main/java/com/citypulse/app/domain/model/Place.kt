package com.citypulse.app.domain.model

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: Category,
    val photoUrl: String? = null,
    val rating: Float? = null,
    val description: String? = null,
    val phoneNumber: String? = null,
    val website: String? = null,
    val distanceMeters: Float? = null
) {
    fun formattedDistance(): String? {
        return distanceMeters?.let { d ->
            if (d < 1000) "${d.toInt()} m"
            else "${String.format("%.1f", d / 1000)} km"
        }
    }

    fun mapsUrl(): String =
        "https://maps.google.com/?q=$latitude,$longitude"

    fun shareText(): String =
        "$name\n$address\nGPS : $latitude, $longitude\n${mapsUrl()}"
}