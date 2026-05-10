package com.citypulse.app.data.remote

import com.citypulse.app.data.remote.dto.PlaceDetailDto
import com.citypulse.app.data.remote.dto.PlaceFeatureDto
import com.citypulse.app.domain.model.Category
import com.citypulse.app.domain.model.Place

fun List<PlaceFeatureDto>.toDomainList(): List<Place> =
    mapNotNull { feature ->
        if (feature.properties.name.isBlank()) null
        else feature.toDomain()
    }

fun PlaceFeatureDto.toDomain(): Place = Place(
    id = properties.xid,
    name = properties.name.trim(),
    address = "",
    latitude = geometry.latitude,
    longitude = geometry.longitude,
    category = kindsToCategory(properties.kinds),
    rating = properties.rate?.toFloat()?.div(7f),
    distanceMeters = properties.dist?.toFloat()
)

fun PlaceDetailDto.toDomain(): Place = Place(
    id = xid,
    name = name.trim(),
    address = address?.formatted() ?: "Adresse inconnue",
    latitude = point.latitude,
    longitude = point.longitude,
    category = kindsToCategory(kinds),
    photoUrl = preview?.imageUrl,
    rating = rate?.toFloat()?.div(7f),
    description = info?.description,
    phoneNumber = info?.phone,
    website = info?.website
)

private fun kindsToCategory(kinds: String): Category {
    val kindsList = kinds.lowercase().split(",").map { it.trim() }
    return when {
        kindsList.any { it.contains("restaurant") || it.contains("food") } -> Category.RESTAURANT
        kindsList.any { it.contains("cafe") || it.contains("coffee") } -> Category.CAFE
        kindsList.any { it.contains("museum") || it.contains("exhibit") } -> Category.MUSEUM
        kindsList.any { it.contains("park") || it.contains("garden") } -> Category.PARK
        kindsList.any { it.contains("hotel") || it.contains("accomodation") } -> Category.HOTEL
        kindsList.any { it.contains("transport") || it.contains("metro") } -> Category.TRANSPORT
        kindsList.any { it.contains("shop") || it.contains("market") } -> Category.SHOP
        else -> Category.OTHER
    }
}