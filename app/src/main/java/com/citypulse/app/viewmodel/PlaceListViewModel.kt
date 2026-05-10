// viewmodel/PlaceListViewModel.kt
package com.citypulse.app.viewmodel

import com.citypulse.app.domain.model.Category
import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.domain.model.Place
import androidx.lifecycle.viewModelScope
import com.citypulse.app.domain.repository.FavoriteRepository
import com.citypulse.app.domain.repository.PlaceRepository
import com.citypulse.app.ui.places.PlaceListEvent
import com.citypulse.app.ui.places.PlaceListUiState
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
/**
 * ViewModel de l'écran liste des lieux.
 * Gère : chargement, filtrage par catégorie, recherche textuelle, favoris.
 */
class PlaceListViewModel(
    private val placeRepository: PlaceRepository,
    private val favoriteRepository: FavoriteRepository,
    private val locationViewModel: LocationViewModel
) : BaseViewModel() {
    // ── État principal de l'UI ────────────────────────────────────────
    private val _uiState = MutableStateFlow<PlaceListUiState>(PlaceListUiState.Loading)
    val uiState: StateFlow<PlaceListUiState> = _uiState.asStateFlow()
    // ── Événements ponctuels ──────────────────────────────────────────
    private val _events = MutableSharedFlow<PlaceListEvent>()
    val events: SharedFlow<PlaceListEvent> = _events.asSharedFlow()
    // ── État interne du filtrage ──────────────────────────────────────
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    private val _searchQuery = MutableStateFlow("")
    private var allPlaces: List<Place> = emptyList() // Cache complet non filtré
    // ── IDs des favoris (Flow observable depuis Room) ─────────────────
    private val favoriteIds: StateFlow<Set<String>> = favoriteRepository
        .getAllFavorites()
        .map { favorites -> favorites.map { it.placeId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    init {
// Observer les changements de filtre et de recherche
        viewModelScope.launch {
            combine(_selectedCategory, _searchQuery) { category, query ->
                Pair(category, query)
            }
                .debounce(300L) // Attendre 300ms après le dernier changement
                .collect { (category, query) ->
                    applyFilters(category, query)
                }
        }
    }
    // ── Charger les lieux ─────────────────────────────────────────────
    fun loadPlaces() {
        val location = locationViewModel.currentLocation.value
        launchSafe {
            _uiState.value = PlaceListUiState.Loading
            val result = if (location != null) {
                placeRepository.getNearbyPlaces(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radiusMeters = 1000
                )
            } else {
// Pas de position — charger le cache Room
                Result.Success(emptyList<Place>())
            }
            when (result) {
                is Result.Success -> {
// Enrichir avec les distances
                    allPlaces = result.data.map { place ->
                        place.copy(
                            distanceMeters = locationViewModel.distanceTo(
                                place.latitude, place.longitude
                            )
                        )
                    }.sortedBy { it.distanceMeters }
                    applyFilters(_selectedCategory.value, _searchQuery.value)
                }
                is Result.Error -> {
                    _uiState.value = PlaceListUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    // ── Filtrage et recherche ─────────────────────────────────────────
    fun filterByCategory(category: Category?) {
        _selectedCategory.value = category
    }
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    private fun applyFilters(category: Category?, query: String) {
        var filtered = allPlaces
// Filtre catégorie
        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }
// Filtre recherche textuelle (insensible à la casse)
        if (query.length >= 2) {
            filtered = filtered.filter { place ->
                place.name.contains(query, ignoreCase = true) ||
                        place.address.contains(query, ignoreCase = true) ||
                        place.category.label.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = if (filtered.isEmpty()) {
            val reason = when {
                query.isNotBlank() -> "Aucun résultat pour '${query}'"
                category != null -> "Aucun lieu de type '${category.label}'"
                else -> "Aucun lieu trouvé à proximité"
            }
            PlaceListUiState.Empty(reason)
        } else {
            PlaceListUiState.Success(filtered)
        }
    }
    // ── Gestion des favoris ───────────────────────────────────────────
    fun toggleFavorite(place: Place) {
        launchSafe {
            val isFavorite = favoriteIds.value.contains(place.id)
            if (isFavorite) {
                favoriteRepository.removeFavorite(place.id)
                _events.emit(PlaceListEvent.ShowMessage("Retiré des favoris"))
            } else {
                favoriteRepository.addFavorite(
                    com.citypulse.app.domain.model.Favorite(place = place)
                )
                _events.emit(PlaceListEvent.ShowMessage("Ajouté aux favoris"))
            }
        }
    }
    // ── Vérifier si un lieu est favori ────────────────────────────────
    fun isFavorite(placeId: String): Boolean = favoriteIds.value.contains(placeId)
}