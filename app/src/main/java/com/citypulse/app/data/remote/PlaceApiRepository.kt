// data/remote/PlaceApiRepository.kt
package com.citypulse.app.data.remote
import com.citypulse.app.domain.model.Place
import com.citypulse.app.domain.repository.PlaceRepository
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


/**
 * Implémentation de PlaceRepository utilisant l'API REST OpenTripMap.
 * Gère tous les types d'erreurs réseau et les convertit en Result.Error.
 *
 * @param apiService Instance Retrofit injectée via NetworkModule
 */
class PlaceApiRepository(private val apiService: ApiService) : PlaceRepository {
    // ── getNearbyPlaces ───────────────────────────────────────────────
    override suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): Result<List<Place>> = safeApiCall {
        val response = apiService.getPlacesNearby(
            latitude = latitude,
            longitude = longitude,
            radius = radiusMeters
        )
        handleResponse(response) { body ->
            body.features.toDomainList()
        }
    }
    // ── searchPlaces ──────────────────────────────────────────────────
    override suspend fun searchPlaces(query: String): Result<List<Place>> {
// La recherche nécessite une position — utiliser Paris par défaut si inconnue
        return safeApiCall {
            val response = apiService.searchPlaces(
                query = query,
                latitude = 48.8566,
                longitude = 2.3522
            )
            handleResponse(response) { body -> body.features.toDomainList() }
        }
    }
    // ── getPlaceById ──────────────────────────────────────────────────
    override suspend fun getPlaceById(id: String): Result<Place> = safeApiCall {
        val response = apiService.getPlaceDetail(xid = id)
        handleResponse(response) { detail -> detail.toDomain() }
    }
    // ── getCachedPlaces / cachePlaces ─────────────────────────────────
// Ces méthodes sont gérées par le repository Room (Jour 5 — P3)
// PlaceApiRepository ne touche pas la base locale
    override fun getCachedPlaces(): Flow<List<Place>> = flowOf(emptyList())
    override suspend fun cachePlaces(places: List<Place>) { /* géré par Room */ }
// ── Helpers internes ──────────────────────────────────────────────
    /**
     * Encapsule un appel API et capture toutes les exceptions.
     * Convertit IOException, HttpException et Exception → Result.Error.
     */
    private suspend fun <T> safeApiCall(block: suspend () -> Result<T>): Result<T> {
        return try {
            block()
        } catch (e: IOException) {
// Pas de réseau, timeout, connexion refusée
            Result.Error(
                message = "Pas de connexion réseau. Vérifiez votre connexion internet.",
                exception = e
            )
        } catch (e: HttpException) {
// Erreur HTTP reçue du serveur
            Result.Error(
                message = httpErrorMessage(e.code()),
                exception = e
            )
        } catch (e: Exception) {
// Erreur inattendue (parsing JSON, etc.)
            Result.Error(
                message = "Erreur inattendue : ${e.message}",
                exception = e
            )
        }
    }
    /**
     * Vérifie isSuccessful() et extrait le body, ou retourne Result.Error.
     */
    private fun <T, R> handleResponse(
        response: Response<T>,
        transform: (T) -> R
    ): Result<R> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.Success(transform(body))
            } else {
                Result.Error("Réponse vide du serveur (HTTP ${response.code()})")
            }
        } else {
            Result.Error(httpErrorMessage(response.code()))
        }
    }
    /**
     * Messages d'erreur HTTP lisibles par l'utilisateur.
     */
    private fun httpErrorMessage(code: Int): String = when (code) {
        400 -> "Requête invalide (erreur 400). Vérifiez les paramètres."
        401 -> "Clé API invalide ou expirée (erreur 401)."
        403 -> "Accès refusé (erreur 403). Quota API peut-être dépassé."
        404 -> "Ressource introuvable (erreur 404)."
        429 -> "Trop de requêtes (erreur 429). Réessayez dans quelques secondes."
        500 -> "Erreur serveur (erreur 500). Réessayez plus tard."
        503 -> "Service temporairement indisponible (erreur 503)."
        else -> "Erreur réseau (HTTP $code)."
    }
}