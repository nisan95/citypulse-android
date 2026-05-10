package com.citypulse.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.citypulse.app.repository.RepositoryProvider // Asire w ou enpòte sa a

class MapViewModelFactory(
    private val locationViewModel: LocationViewModel,
    private val context: Context // Nou ajoute context la isit la
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(
                locationViewModel = locationViewModel,
                // Nou itilize Provider a pou n bay vrè Repository a ✅
                placeRepository = RepositoryProvider.placeRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}