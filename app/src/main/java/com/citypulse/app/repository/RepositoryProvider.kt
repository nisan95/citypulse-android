// repository/RepositoryProvider.kt
package com.citypulse.app.repository
import android.content.Context
import com.citypulse.app.data.local.AppDatabase
import com.citypulse.app.data.remote.NetworkModule
import com.citypulse.app.data.remote.PlaceApiRepository
import com.citypulse.app.domain.repository.FavoriteRepository
import com.citypulse.app.domain.repository.PlaceRepository
/**
 * Point d'entrée unique pour obtenir les repositories.
 * Gère les dépendances et le cycle de vie des instances.
 *
 * Utilisation dans une Factory :
 * val placeRepo = RepositoryProvider.placeRepository(context)
 * val favRepo = RepositoryProvider.favoriteRepository(context)
 *
 * Note : dans un vrai projet en production, remplacer par Hilt.
 */
object RepositoryProvider {
    fun placeRepository(context: Context): PlaceRepository {
        val db = AppDatabase.getInstance(context)
        val apiService = NetworkModule.provideApiService(context, isDebug = true)
        val apiRepo = PlaceApiRepository(apiService)
        return PlaceRepositoryImpl(
            placeDao = db.placeDao(),
            apiRepository = apiRepo
        )
    }

    fun favoriteRepository(context: Context): FavoriteRepository {
        val db = AppDatabase.getInstance(context)
        return FavoriteRepositoryImpl(
            favoriteDao = db.favoriteDao(),
            placeDao = db.placeDao()
        )
    }
}