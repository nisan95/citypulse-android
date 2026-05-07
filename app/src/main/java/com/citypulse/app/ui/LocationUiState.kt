// ui/LocationUiState.kt
package com.citypulse.app.ui
import android.location.Location
/**
 * Tous les états possibles de l'écran carte.
 * Le Fragment fait un when(state) { ... } dessus.
 *
 * Flux normal : RequestingPermission → Locating → Located
 * Flux d'erreur : RequestingPermission → PermissionDenied
 * Locating → GpsUnavailable
 */
sealed class LocationUiState {
    object RequestingPermission : LocationUiState() // En attente permission
    object Locating : LocationUiState() // GPS en acquisition
    data class Located(val location: Location) : LocationUiState() // Position OK
    object PermissionDenied : LocationUiState() // Permission refusée
}