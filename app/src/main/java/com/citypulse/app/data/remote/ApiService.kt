// data/remote/ApiService.kt
package com.citypulse.app.data.remote
import com.citypulse.app.data.remote.dto.NearbyPlacesResponse
import com.citypulse.app.data.remote.dto.PlaceDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
/**
 * Interface Retrofit pour l'API OpenTripMap.
 * Retrofit génère l'implémentation à partir des annotations.
 *
 * Documentation API : https://opentripmap.io/docs
 * BaseURL : https://api.opentripmap.com/0.1/
 */
interface ApiService {
// ── Endpoint 1 : lieux à proximité ────────────────────────────────
    /**
     * Retourne les lieux d'intérêt dans un rayon donné.
     * GET /en/places/radius?radius=1000&lon=2.35&lat=48.85&limit=20&apikey=...
     */
    @GET("en/places/radius")
    suspend fun getPlacesNearby(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radius: Int = NetworkConstants.DEFAULT_RADIUS_M,
        @Query("limit") limit: Int = NetworkConstants.DEFAULT_PAGE_SIZE,
        @Query("apikey") apiKey: String = NetworkConstants.API_KEY
    ): Response<NearbyPlacesResponse>
// ── Endpoint 2 : détail d'un lieu ─────────────────────────────────
    /**
     * Retourne le détail complet d'un lieu par son XID.
     * GET /en/places/xid/{xid}?apikey=...
     */
    @GET("en/places/xid/{xid}")
    suspend fun getPlaceDetail(
        @Path("xid") xid: String,
        @Query("apikey") apiKey: String = NetworkConstants.API_KEY
    ): Response<PlaceDetailDto>
// ── Endpoint 3 : recherche textuelle ──────────────────────────────
    /**
     * Recherche des lieux par nom.
     * GET /en/places/autosuggest?q=louvre&lon=2.35&lat=48.85&limit=10&apikey=...
     */
    @GET("en/places/autosuggest")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 10,
        @Query("apikey") apiKey: String = NetworkConstants.API_KEY
    ): Response<NearbyPlacesResponse>
}