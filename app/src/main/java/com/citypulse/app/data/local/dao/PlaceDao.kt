package com.citypulse.app.data.local.dao

import androidx.room.*
import com.citypulse.app.data.local.entities.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY name ASC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE category = :category ORDER BY name ASC")
    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPlaces(query: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: String): PlaceEntity?

    @Query("SELECT * FROM places WHERE cached_at > :since ORDER BY name ASC")
    suspend fun getPlacesCachedAfter(since: Long): List<PlaceEntity>

    @Query("SELECT COUNT(*) FROM places")
    suspend fun countPlaces(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Update
    suspend fun updatePlace(place: PlaceEntity)

    @Delete
    suspend fun deletePlace(place: PlaceEntity)

    @Query("DELETE FROM places WHERE cached_at < :before")
    suspend fun deletePlacesCachedBefore(before: Long): Int

    @Query("DELETE FROM places")
    suspend fun clearAllPlaces()
}