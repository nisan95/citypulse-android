
// viewmodel/LocationViewModel.kt
package com.citypulse.app.viewmodel
import android.location.Location
import androidx.lifecycle.viewModelScope
import com.citypulse.app.domain.repository.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
/**
 * ViewModel partagé entre tous les écrans nécessitant la position GPS.
 * Utiliser by activityViewModels() pour le partager entre Fragments.
 * Utiliser by viewModels() pour un usage isolé dans un Fragment.
 */
class LocationViewModel(private val locationRepository: LocationRepository) : BaseViewModel() {
    // ── Position actuelle (GPS en temps réel) ─────────────────────────
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    // ── Dernière position connue (chargée rapidement au démarrage) ────
    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation: StateFlow<Location?> = _lastKnownLocation.asStateFlow()
    // ── Disponibilité GPS ─────────────────────────────────────────────
    private val _isGpsAvailable = MutableStateFlow(true)
    val isGpsAvailable: StateFlow<Boolean> = _isGpsAvailable.asStateFlow()
    private var locationJob: Job? = null
    // ── Démarrer le suivi GPS ─────────────────────────────────────────
    fun startLocationUpdates() {
        if (locationJob?.isActive == true) return // Déjà actif
        locationJob = viewModelScope.launch {
            locationRepository.locationUpdates()
                .catch { e ->
                    _isGpsAvailable.value = false
                    emitError(e.message ?: "Erreur GPS")
                }
                .collect { location ->
                    _isGpsAvailable.value = true
                    _currentLocation.value = location
                }
        }
    }
    // ── Arrêter le suivi GPS (appeler dans onPause) ───────────────────
    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }
    // ── Récupérer rapidement la dernière position connue ──────────────
    fun fetchLastKnownLocation() = launchSafe {
        val loc = locationRepository.getLastLocation()
        _lastKnownLocation.value = loc
        if (_currentLocation.value == null) _currentLocation.value = loc
    }
// ── Utilitaires d'affichage ───────────────────────────────────────
    /** Format : '48.856600, 2.352200 (±12 m)' */
    fun formatLocation(location: Location? = _currentLocation.value): String? =
        location?.let { "%.6f, %.6f (±%.0f m)".format(it.latitude, it.longitude, it.accuracy) }
    /** Distance en mètres vers un point donné, null si position inconnue */
    fun distanceTo(lat: Double, lng: Double): Float? {
        val current = _currentLocation.value ?: return null
        val target = Location("").apply { latitude = lat; longitude = lng }
        return locationRepository.distanceBetween(current, target)
    }
    /** Distance formatée : '350 m' ou '2.3 km' */
    fun formattedDistanceTo(lat: Double, lng: Double): String? =
        distanceTo(lat, lng)?.let { m ->
            if (m < 1000) "${m.toInt()} m"
            else "${ "%.1f".format(m / 1000) } km"
        }
    override fun onCleared() { super.onCleared(); stopLocationUpdates() }
}