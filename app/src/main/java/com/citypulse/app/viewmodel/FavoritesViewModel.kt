// viewmodel/FavoritesViewModel.kt
package com.citypulse.app.viewmodel

import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.domain.repository.FavoriteRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
/**
 * ViewModel de l'ecran favoris.
 * Expose la liste des favoris depuis Room (Flow observable).
 * Gere la suppression avec possibilite d'annulation (undo).
 */
class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository
) : BaseViewModel() {
    // ── Liste des favoris -- Flow depuis Room (mise a jour automatique) ─
    val favorites: StateFlow<List<Favorite>> = favoriteRepository
        .getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // ── Evenements ponctuels ──────────────────────────────────────────
    private val _events = MutableSharedFlow<FavoritesEvent>()
    val events: SharedFlow<FavoritesEvent> = _events.asSharedFlow()
    // Favori supprime temporairement (pour le Snackbar Annuler)
    private var lastDeletedFavorite: Favorite? = null
    // ── Supprimer un favori ───────────────────────────────────────────
    fun deleteFavorite(favorite: Favorite) {
        lastDeletedFavorite = favorite
        launchSafe {
            favoriteRepository.removeFavorite(favorite.placeId)
            _events.emit(FavoritesEvent.ShowUndo(favorite.place.name))
        }
    }
    // ── Annuler la derniere suppression ───────────────────────────────
    fun undoDelete() {
        val deleted = lastDeletedFavorite ?: return
        launchSafe {
            favoriteRepository.addFavorite(deleted)
            lastDeletedFavorite = null
        }
    }
    // ── Navigation vers le detail ─────────────────────────────────────
    fun onFavoriteClicked(favorite: Favorite) {
        launchSafe { _events.emit(FavoritesEvent.NavigateToDetail(favorite.placeId)) }
    }
}
sealed class FavoritesEvent {
    data class ShowUndo(val placeName: String) : FavoritesEvent()
    data class NavigateToDetail(val placeId: String): FavoritesEvent()
}