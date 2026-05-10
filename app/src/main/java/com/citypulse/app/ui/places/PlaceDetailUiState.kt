package com.citypulse.app.ui.places

import com.citypulse.app.domain.model.Place

sealed class PlaceDetailUiState {
    object Loading : PlaceDetailUiState()
    data class Success(
        val place: Place,
        val distanceFormatted: String?,
        val savedNote: String = ""
    ) : PlaceDetailUiState()
    data class Error(val message: String) : PlaceDetailUiState()
}

sealed class PlaceDetailEvent {
    object FavoriteAdded : PlaceDetailEvent()
    object FavoriteRemoved : PlaceDetailEvent()
    object NoteSaved : PlaceDetailEvent()
    data class ShareRequested(val text: String) : PlaceDetailEvent()
    data class ShowError(val message: String) : PlaceDetailEvent()
}