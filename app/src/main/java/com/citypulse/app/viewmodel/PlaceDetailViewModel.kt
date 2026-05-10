package com.citypulse.app.viewmodel

import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.domain.model.Place
import com.citypulse.app.domain.repository.FavoriteRepository
import com.citypulse.app.domain.repository.PlaceRepository
import com.citypulse.app.ui.places.PlaceDetailEvent
import com.citypulse.app.ui.places.PlaceDetailUiState
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import androidx.lifecycle.viewModelScope

class PlaceDetailViewModel(
    private val placeId: String,
    private val placeRepository: PlaceRepository,
    private val favoriteRepository: FavoriteRepository,
    private val locationViewModel: LocationViewModel
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PlaceDetailUiState>(PlaceDetailUiState.Loading)
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PlaceDetailEvent>()
    val events: SharedFlow<PlaceDetailEvent> = _events.asSharedFlow()

    private val _currentPlace = MutableStateFlow<Place?>(null)
    val currentPlace: StateFlow<Place?> = _currentPlace.asStateFlow()

    val isFavorite: StateFlow<Boolean> = favoriteRepository
        .isFavorite(placeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _noteInput = MutableStateFlow("")

    init {
        loadPlace()
        setupNoteDebounce()
    }

    private fun loadPlace() {
        launchSafe {
            _uiState.value = PlaceDetailUiState.Loading
            when (val result = placeRepository.getPlaceById(placeId)) {
                is Result.Success -> {
                    val place = result.data
                    _currentPlace.value = place
                    val savedNote = favoriteRepository
                        .getAllFavorites()
                        .first()
                        .find { it.placeId == placeId }
                        ?.note ?: ""
                    _uiState.value = PlaceDetailUiState.Success(
                        place = place,
                        distanceFormatted = locationViewModel.formattedDistanceTo(
                            place.latitude, place.longitude
                        ),
                        savedNote = savedNote
                    )
                }
                is Result.Error -> {
                    _uiState.value = PlaceDetailUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun toggleFavorite() {
        val place = _currentPlace.value ?: return
        launchSafe {
            if (isFavorite.value) {
                when (favoriteRepository.removeFavorite(placeId)) {
                    is Result.Success -> _events.emit(PlaceDetailEvent.FavoriteRemoved)
                    is Result.Error -> _events.emit(PlaceDetailEvent.ShowError("Impossible de retirer le favori"))
                    else -> {}
                }
            } else {
                val favorite = Favorite(place = place, note = _noteInput.value)
                when (favoriteRepository.addFavorite(favorite)) {
                    is Result.Success -> _events.emit(PlaceDetailEvent.FavoriteAdded)
                    is Result.Error -> _events.emit(PlaceDetailEvent.ShowError("Impossible d'ajouter le favori"))
                    else -> {}
                }
            }
        }
    }

    fun onNoteChanged(text: String) {
        _noteInput.value = text
    }

    private fun setupNoteDebounce() {
        viewModelScope.launch {
            _noteInput
                .drop(1)
                .debounce(1000L)
                .collect { note ->
                    if (isFavorite.value) {
                        when (favoriteRepository.updateNote(placeId, note)) {
                            is Result.Success -> _events.emit(PlaceDetailEvent.NoteSaved)
                            else -> {}
                        }
                    }
                }
        }
    }

    fun onShareRequested() {
        val place = _currentPlace.value ?: return
        launchSafe { _events.emit(PlaceDetailEvent.ShareRequested(place.shareText())) }
    }
}