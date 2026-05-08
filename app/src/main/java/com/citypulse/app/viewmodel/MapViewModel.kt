package com.citypulse.app.viewmodel

import android.location.Location
import com.citypulse.app.domain.model.Place
import com.citypulse.app.domain.repository.PlaceRepository
import com.citypulse.app.ui.map.MapUiState
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel(private val placeRepository: PlaceRepository) : BaseViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var lastLoadedLocation: Location? = null
    private val RELOAD_THRESHOLD_METERS = 500

    fun loadNearbyPlaces(latitude: Double, longitude: Double, radiusMeters: Int = 1000) {
        launchSafe {
            _uiState.value = MapUiState.Loading
            when (val result = placeRepository.getNearbyPlaces(latitude, longitude, radiusMeters)) {
                is Result.Success -> {
                    _uiState.value = MapUiState.PlacesLoaded(result.data)
                }
                is Result.Error -> {
                    _uiState.value = MapUiState.Error(result.message)
                    loadFromCache()
                }
                else -> {}
            }
        }
    }

    fun onLocationUpdated(newLocation: Location) {
        val last = lastLoadedLocation
        if (last == null || last.distanceTo(newLocation) > RELOAD_THRESHOLD_METERS) {
            lastLoadedLocation = newLocation
            loadNearbyPlaces(newLocation.latitude, newLocation.longitude)
        }
    }

    fun onPlaceSelected(place: Place) {
        _uiState.value = MapUiState.NavigateToDetail(place)
    }

    private suspend fun loadFromCache() {
        placeRepository.getCachedPlaces().collect { cachedPlaces ->
            if (cachedPlaces.isNotEmpty()) {
                _uiState.value = MapUiState.PlacesLoaded(cachedPlaces)
            }
        }
    }

    fun formatDistance(currentLocation: Location?, place: Place): String {
        currentLocation ?: return ""
        val target = Location("").apply {
            latitude = place.latitude
            longitude = place.longitude
        }
        val meters = currentLocation.distanceTo(target)
        return if (meters < 1000) "${meters.toInt()} m"
        else "${"%.1f".format(meters / 1000)} km"
    }
}