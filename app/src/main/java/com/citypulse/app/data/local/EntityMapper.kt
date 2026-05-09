//data/local/EntityMapper.kt
package com.citypulse.app.data.local
import com.citypulse.app.data.local.entities.FavoriteEntity
import com.citypulse.app.data.local.entities.PlaceEntity
import com.citypulse.app.domain.model.Category
import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.domain.model.Place
//──PlaceEntity→Place(Domain)──────────────────────────────────────
fun PlaceEntity.toDomain():Place=Place(
id =id,
name =name,
address =address,
latitude =latitude,
longitude =longitude,
category =Category.fromString(category),
photoUrl =photoUrl,
rating =rating,
description=description,
phoneNumber=phoneNumber,
website =website
)
fun List<PlaceEntity>.toDomainList(): List<Place> = map{it.toDomain()}
//──Place(Domain)→PlaceEntity──────────────────────────────────────
fun Place.toEntity():PlaceEntity=PlaceEntity(
id =id,
name =name,
address =address,
latitude =latitude,
longitude =longitude,
category =category.name,
photoUrl =photoUrl,
rating =rating,
description=description,
phoneNumber=phoneNumber,
website =website,
cachedAt =System.currentTimeMillis()
)
fun List<Place>.toEntityList(): List<PlaceEntity> = map{it.toEntity()}
//──FavoriteEntity→Favorite(Domain)────────────────────────────────
//NécessitelePlacecorrespondant(jointureeffectuéedansleDAOouRepository)
fun FavoriteEntity.toDomain(place:Place): Favorite = Favorite(
place =place,
note =note,
addedAt=addedAt
)
//──Favorite(Domain)→FavoriteEntity────────────────────────────────
fun Favorite.toEntity(): FavoriteEntity = FavoriteEntity(
placeId=placeId,
note =note,
addedAt=addedAt
)