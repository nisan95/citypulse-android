// ui/places/PlaceListUiState.kt
package com.citypulse.app.ui.places
import com.citypulse.app.domain.model.Place
/**
 * États de l'écran liste des lieux.
 * Observé par PlaceListFragment depuis PlaceListViewModel.uiState.
 */
sealed class PlaceListUiState {
    object Loading : PlaceListUiState()
    data class Success(val places: List<Place>) : PlaceListUiState()
    data class Empty(val reason: String = "Aucun lieu trouvé") : PlaceListUiState()
    data class Error(val message: String) : PlaceListUiState()
}
/**
 * Événements ponctuels émis par PlaceListViewModel.
 * Utilisés pour la navigation et les messages (Snackbar).
 */
sealed class PlaceListEvent {
    data class NavigateToDetail(val place: Place) : PlaceListEvent()
    data class ShowMessage(val message: String) : PlaceListEvent()
}