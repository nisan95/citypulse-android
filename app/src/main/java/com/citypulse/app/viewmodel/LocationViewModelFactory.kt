// viewmodel/LocationViewModelFactory.kt
package com.citypulse.app.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.citypulse.app.domain.repository.LocationRepository
/**
 * Factory pour LocationViewModel — injection manuelle du Repository.
 *
 * Dans un Fragment (ViewModel privé) :
 * private val vm: LocationViewModel by viewModels {
 * LocationViewModelFactory(FusedLocationRepository(requireContext()))
 * }
 *
 * Entre Fragments (ViewModel partagé) :
 * private val vm: LocationViewModel by activityViewModels {
 * LocationViewModelFactory(FusedLocationRepository(requireContext()))
 * }
 */
class LocationViewModelFactory(
    private val repo: LocationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java))
            return LocationViewModel(repo) as T
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}