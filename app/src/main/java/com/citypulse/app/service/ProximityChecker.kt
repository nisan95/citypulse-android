package com.citypulse.app.service
import android.content.Context
import android.location.Location
import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.util.NotificationHelper
/**
 * Verifie si la position courante est proche d un lieu favori.
 * Declenche une notification si la distance est inferieure au seuil.
 *
 * Utilise par LocationForegroundService a chaque mise a jour GPS.
 * Gere la deduplication : une seule notification par lieu par session.
 */
class ProximityChecker(private val context: Context) {
    companion object {
        // Seuil de proximite en metres
        const val PROXIMITY_THRESHOLD_METERS = 500f
        // Delai minimum entre deux notifications pour le meme lieu (30 min)
        private const val NOTIFICATION_COOLDOWN_MS = 30 * 60 * 1000L
    }
    // Stocke les IDs des lieux notifies et leur timestamp
// Cle = placeId, Valeur = timestamp de la derniere notification
    private val notifiedPlaces = mutableMapOf<String, Long>()
// ── Verifier la proximite avec une liste de favoris ───────────────
    /**
     * Appele depuis LocationForegroundService a chaque update GPS.
     * @param currentLocation Position GPS actuelle
     * @param favorites Liste des lieux favoris a verifier
     */
    fun checkProximity(
        currentLocation: Location,
        favorites: List<Favorite>
    ) {
        val now = System.currentTimeMillis()
        favorites.forEach { favorite ->
            val place = favorite.place
// Construire une Location Android pour le lieu favori
            val placeLocation = Location("").apply {
                latitude = place.latitude
                longitude = place.longitude
            }
// Calculer la distance en metres (formule Haversine integree Android)
            val distanceMeters = currentLocation.distanceTo(placeLocation)
            if (distanceMeters <= PROXIMITY_THRESHOLD_METERS) {
// Verifier le cooldown pour eviter le spam de notifications
                val lastNotified = notifiedPlaces[place.id] ?: 0L
                val cooldownExpired = (now - lastNotified) >= NOTIFICATION_COOLDOWN_MS
                if (cooldownExpired) {
// Envoyer la notification
                    val distanceFormatted = formatDistance(distanceMeters)
                    NotificationHelper.sendProximityNotification(
                        context = context,
                        place = place,
                        distance = distanceFormatted
                    )
// Enregistrer le timestamp pour le cooldown
                    notifiedPlaces[place.id] = now
                }
            }
        }
    }
// ── Reinitialiser les notifications vues ──────────────────────────
    /** Appeler quand l utilisateur quitte la zone d un lieu */
    fun resetPlace(placeId: String) {
        notifiedPlaces.remove(placeId)
    }
    /** Reinitialiser tous les cooldowns (ex : au demarrage de l app) */
    fun resetAll() {
        notifiedPlaces.clear()
    }
    // ── Formater la distance pour l affichage ─────────────────────────
    private fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            "%.1f km".format(meters / 1000)
        }
    }
}