package com.citypulse.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlaceDetailDto(
    @SerializedName("xid") val xid: String,
    @SerializedName("name") val name: String,
    @SerializedName("kinds") val kinds: String,
    @SerializedName("rate") val rate: Int?,
    @SerializedName("point") val point: PointDto,
    @SerializedName("address") val address: AddressDto?,
    @SerializedName("preview") val preview: PreviewDto?,
    @SerializedName("info") val info: InfoDto?
)

data class PointDto(
    @SerializedName("lon") val longitude: Double,
    @SerializedName("lat") val latitude: Double
)

data class AddressDto(
    @SerializedName("road") val road: String?,
    @SerializedName("house_number") val houseNumber: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("country") val country: String?
) {
    fun formatted(): String {
        val parts = listOfNotNull(houseNumber, road, city, postcode)
        return parts.joinToString(", ").ifBlank { "Adresse inconnue" }
    }
}

data class PreviewDto(
    @SerializedName("source") val imageUrl: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?
)

data class InfoDto(
    @SerializedName("descr") val description: String?,
    @SerializedName("url") val website: String?,
    @SerializedName("phone") val phone: String?
)