package com.citypulse.app.data.local.dao

import androidx.room.*
import com.citypulse.app.data.local.entities.FavoriteEntity
import com.citypulse.app.data.local.entities.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT place_id FROM favorites ORDER BY added_at DESC")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT * FROM favorites ORDER BY added_at DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE place_id = :placeId LIMIT 1")
    suspend fun getFavoriteByPlaceId(placeId: String): FavoriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE place_id = :placeId)")
    fun isFavorite(placeId: String): Flow<Boolean>

    @Transaction
    @Query("""
        SELECT places.* FROM places
        INNER JOIN favorites ON places.id = favorites.place_id
        ORDER BY favorites.added_at DESC
    """)
    fun getFavoritePlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun countFavorites(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Update
    suspend fun updateFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE place_id = :placeId")
    suspend fun deleteFavoriteByPlaceId(placeId: String): Int

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("UPDATE favorites SET note = :note WHERE place_id = :placeId")
    suspend fun updateNote(placeId: String, note: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
}