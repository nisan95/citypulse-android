package com.citypulse.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NearbyPlacesResponse(
    @SerializedName("type") val type: String,
    @SerializedName("features") val features: List<PlaceFeatureDto>
)

data class PlaceFeatureDto(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("properties") val properties: PlacePropertiesDto,
    @SerializedName("geometry") val geometry: PlaceGeometryDto
)

data class PlacePropertiesDto(
    @SerializedName("xid") val xid: String,
    @SerializedName("name") val name: String,
    @SerializedName("rate") val rate: Int?,
    @SerializedName("kinds") val kinds: String,
    @SerializedName("dist") val dist: Double?
)

data class PlaceGeometryDto(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<Double>
) {
    val longitude: Double get() = coordinates.getOrElse(0) { 0.0 }
    val latitude: Double get() = coordinates.getOrElse(1) { 0.0 }
}