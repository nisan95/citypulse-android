// viewmodel/FavoritesViewModelFactory.kt
package com.citypulse.app.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.citypulse.app.domain.repository.FavoriteRepository
class FavoritesViewModelFactory(
    private val favoriteRepository: FavoriteRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java))
            return FavoritesViewModel(favoriteRepository) as T
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}