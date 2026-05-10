// viewmodel/PlaceListViewModelFactory.kt
package com.citypulse.app.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.citypulse.app.domain.repository.FavoriteRepository
import com.citypulse.app.domain.repository.PlaceRepository
class PlaceListViewModelFactory(
    private val placeRepository: PlaceRepository,
    private val favoriteRepository: FavoriteRepository,
    private val locationViewModel: LocationViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceListViewModel::class.java)) {
            return PlaceListViewModel(
                placeRepository = placeRepository,
                favoriteRepository = favoriteRepository,
                locationViewModel = locationViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}