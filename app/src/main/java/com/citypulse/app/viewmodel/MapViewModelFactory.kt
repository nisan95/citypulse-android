package com.citypulse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.citypulse.app.domain.repository.PlaceRepository

class MapViewModelFactory(
    private val placeRepository: PlaceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java))
            return MapViewModel(placeRepository) as T
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}