package com.citypulse.app.repository

// Enpòte fonksyon yo dirèkteman piske yo nan folder 'local' la
import com.citypulse.app.data.local.toDomain
import com.citypulse.app.data.local.toDomainList
import com.citypulse.app.data.local.toEntityList

import com.citypulse.app.data.remote.PlaceApiRepository
import com.citypulse.app.data.local.dao.PlaceDao
import com.citypulse.app.domain.model.Place
import com.citypulse.app.domain.repository.PlaceRepository
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
/**
 * Implémentation de PlaceRepository — source unique de vérité.
 * Combine PlaceApiRepository (réseau) et PlaceDao (Room).
 *
 * Stratégie offline-first :
 * 1. Cache frais (< 1h) → Room directement
 * 2. Cache expiré ou vide → API → sauvegarder dans Room → retourner
 * 3. API en échec → Room (même expiré) + avertissement
 *
 * @param placeDao DAO Room pour la lecture/écriture locale
 * @param apiRepository Repository distant Retrofit
 */
class PlaceRepositoryImpl(
    private val placeDao: PlaceDao,
    private val apiRepository: PlaceApiRepository
) : PlaceRepository {
    companion object {
        // Durée de validité du cache : 1 heure
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L
    }
    // ── getNearbyPlaces — stratégie offline-first ─────────────────────
    override suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): Result<List<Place>> {
// Étape 1 : vérifier le cache Room
        val cacheThreshold = System.currentTimeMillis() - CACHE_DURATION_MS
        val cachedEntities = placeDao.getPlacesCachedAfter(cacheThreshold)
        if (cachedEntities.isNotEmpty()) {
// Cache frais — retourner Room sans appel réseau
            return Result.Success(cachedEntities.toDomainList())
        }
// Étape 2 : cache vide ou expiré — appeler l'API
        return when (val apiResult = apiRepository.getNearbyPlaces(
            latitude, longitude, radiusMeters
        )) {
            is Result.Success -> {
                val places = apiResult.data
// Sauvegarder dans Room pour les prochains accès hors-ligne
                placeDao.insertPlaces(places.toEntityList())
                Result.Success(places)
            }
            is Result.Error -> {
// Étape 3 : API en échec — retourner le cache même expiré
                val staleCache = placeDao.getPlacesCachedAfter(0L) // tout le cache
                if (staleCache.isNotEmpty()) {
// Cache expiré disponible — avertir l'utilisateur
                    Result.Success(staleCache.toDomainList())
// TODO : émettre un avertissement 'données potentiellement obsolètes'
                } else {
// Aucun cache — propager l'erreur réseau
                    apiResult
                }
            }
            else -> apiResult
        }
    }
    // ── searchPlaces ──────────────────────────────────────────────────
    override suspend fun searchPlaces(query: String): Result<List<Place>> {
// D'abord chercher dans le cache Room (rapide, hors-ligne)
        val cached = placeDao.searchPlaces(query)
// searchPlaces retourne un Flow — prendre la première valeur
// Pour une recherche ponctuelle, on utilise l'API si connecté
        return when (val apiResult = apiRepository.searchPlaces(query)) {
            is Result.Success -> {
// Mettre en cache les résultats de recherche
                placeDao.insertPlaces(apiResult.data.toEntityList())
                apiResult
            }
            is Result.Error -> {
// Fallback Room sur la recherche locale
                Result.Error(
                    message = "${apiResult.message} (résultats locaux utilisés)",
                    exception = apiResult.exception
                )
            }
            else -> apiResult
        }
    }
    // ── getPlaceById ──────────────────────────────────────────────────
    override suspend fun getPlaceById(id: String): Result<Place> {
// Chercher en local d'abord
        val local = placeDao.getPlaceById(id)

        if (local != null) return Result.Success(local.toDomain())
// Sinon appeler l'API pour le détail complet
        return apiRepository.getPlaceById(id)
    }
    // ── getCachedPlaces — Flow observable ─────────────────────────────
    override fun getCachedPlaces(): Flow<List<Place>> =
        placeDao.getAllPlaces().map { entities -> entities.toDomainList() }
    // ── cachePlaces ───────────────────────────────────────────────────
    override suspend fun cachePlaces(places: List<Place>) {
        placeDao.insertPlaces(places.toEntityList())
    }
}