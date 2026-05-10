// repository/FavoriteRepositoryImpl.kt
package com.citypulse.app.repository


// Nou rele fonksyon yo dirèkteman nan package local la
import com.citypulse.app.data.local.toDomain
import com.citypulse.app.data.local.toDomainList
import com.citypulse.app.data.local.toEntity

import com.citypulse.app.data.local.dao.FavoriteDao
import com.citypulse.app.data.local.dao.PlaceDao
import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.domain.repository.FavoriteRepository
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
/**
 * Implémentation de FavoriteRepository — uniquement Room, pas de réseau.
 * Les favoris sont une donnée purement locale et persistante.
 */
class FavoriteRepositoryImpl(
    private val favoriteDao: FavoriteDao,
    private val placeDao: PlaceDao
) : FavoriteRepository {
    // ── getAllFavorites ────────────────────────────────────────────────
    override fun getAllFavorites(): Flow<List<Favorite>> {
// Jointure via combine : écouter les deux Flow ensemble
        return combine(
            favoriteDao.getAllFavorites(),
            favoriteDao.getFavoritePlaces()
        ) { favoriteEntities, placeEntities ->
            val placesById = placeEntities.associateBy { it.id }
            favoriteEntities.mapNotNull { favEntity ->
                val placeEntity = placesById[favEntity.placeId] ?: return@mapNotNull null
                favEntity.toDomain(placeEntity.toDomain())
            }
        }
    }
    // ── isFavorite ────────────────────────────────────────────────────
    override fun isFavorite(placeId: String): Flow<Boolean> =
        favoriteDao.isFavorite(placeId)
    // ── addFavorite ───────────────────────────────────────────────────
    override suspend fun addFavorite(favorite: Favorite): Result<Unit> {
        return try {
// Vérifier que le lieu existe dans Room (sinon FK violation)
            val placeExists = placeDao.getPlaceById(favorite.placeId) != null
            if (!placeExists) {
// Insérer le lieu si absent (peut arriver si cache expiré supprimé)
                placeDao.insertPlace(favorite.place.toEntity())
            }
            val id = favoriteDao.insertFavorite(favorite.toEntity())
            if (id != -1L) Result.Success(Unit)
            else Result.Error("Ce lieu est déjà en favori")
        } catch (e: Exception) {
            Result.Error("Impossible d'ajouter le favori : ${e.message}", e)
        }
    }
    // ── removeFavorite ────────────────────────────────────────────────
    override suspend fun removeFavorite(placeId: String): Result<Unit> {
        return try {
            val deleted = favoriteDao.deleteFavoriteByPlaceId(placeId)
            if (deleted > 0) Result.Success(Unit)
            else Result.Error("Favori introuvable")
        } catch (e: Exception) {
            Result.Error("Impossible de supprimer le favori : ${e.message}", e)
        }
    }
    // ── updateNote ────────────────────────────────────────────────────
    override suspend fun updateNote(placeId: String, note: String): Result<Unit> {
        return try {
            favoriteDao.updateNote(placeId, note)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Impossible de sauvegarder la note : ${e.message}", e)
        }
    }
}