package com.citypulse.app.domain.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    /** Flow continu de positions — émet à chaque update GPS */
    fun locationUpdates(): Flow<Location>

    /** Dernière position connue (rapide, sans attente) */
    suspend fun getLastLocation(): Location?

    /** Distance en mètres entre deux positions */
    fun distanceBetween(from: Location, to: Location): Float
}