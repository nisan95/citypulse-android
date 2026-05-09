//data/local/entities/PlaceEntity.kt
package com.citypulse.app.data.local.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
/**
 *EntitéRoomreprésentantlatable'places'.
 *Correspondaucachelocaldeslieuxrécupérésdepuisl'API.
 *
 *Indexsurcategory:accélèrelesrequêtesdefiltrageparcatégorie.
 */
@Entity(
    tableName="places",
    indices=[
        Index(value=["category"]),
        Index(value=["cached_at"])
    ]
)
data class PlaceEntity(
@PrimaryKey
@ColumnInfo(name="id")
val id:String,
@ColumnInfo(name="name")
val name:String,
@ColumnInfo(name="address")
val address:String,
@ColumnInfo(name="latitude")
val latitude:Double,
@ColumnInfo(name="longitude")
val longitude:Double,
@ColumnInfo(name="category")
val category:String, //StockéenStringviaConverters
@ColumnInfo(name="photo_url")
val photoUrl:String?,
@ColumnInfo(name="rating")
val rating:Float?,
@ColumnInfo(name="description")
val description:String?,
@ColumnInfo(name="phone_number")
val phoneNumber:String?,
@ColumnInfo(name="website")
val website:String?,
//Timestampdemiseencache—utilisépourinvaliderlecacheaprès1h
@ColumnInfo(name="cached_at")
val cachedAt:Long=System.currentTimeMillis()
)