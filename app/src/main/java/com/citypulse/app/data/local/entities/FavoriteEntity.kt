//data/local/entities/FavoriteEntity.kt
package com.citypulse.app.data.local.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
/**
 *EntitéRoomreprésentantlatable'favorites'.
 *Stockeleslieuxmisenfavorisparl'utilisateuravecleurnotepersonnelle.
 *
 *Cléétrangèrevers'places' :
 *-onDelete=CASCADE:si lelieuestsuppriméducache, lefavori l'estaussi.
 *-Indexsurplace_id:accélèrelesjointuresetlookups.
 */
@Entity(
    tableName="favorites",
    foreignKeys=[ForeignKey(
        entity =PlaceEntity::class,
        parentColumns=["id"],
        childColumns=["place_id"],
        onDelete =ForeignKey.CASCADE
    )],
    indices=[Index(value=["place_id"],unique=true)]
)
data class FavoriteEntity(
//IDauto-généréparRoom
@PrimaryKey(autoGenerate=true)
@ColumnInfo(name="id")
val id:Long=0,
//Référenceverslatableplaces
@ColumnInfo(name="place_id")
val placeId:String,
//Notepersonnelledel'utilisateur(videpardéfaut)
@ColumnInfo(name="note")
val note:String="",
//Dated'ajoutauxfavoris
@ColumnInfo(name="added_at")
val addedAt:Long=System.currentTimeMillis()
)