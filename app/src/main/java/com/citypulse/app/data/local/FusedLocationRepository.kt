package com.citypulse.app.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.citypulse.app.domain.repository.LocationRepository
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FusedLocationRepository(private val context: Context) : LocationRepository {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS
    ).apply {
        setMinUpdateIntervalMillis(MIN_INTERVAL_MS)
        setMaxUpdateDelayMillis(MAX_DELAY_MS)
        setWaitForAccurateLocation(false)
    }.build()

    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
            override fun onLocationAvailability(avail: LocationAvailability) {}
        }
        fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? = try {
        val token = CancellationTokenSource()
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token
        ).await()
    } catch (e: Exception) {
        fusedClient.lastLocation.await()
    }

    override fun distanceBetween(from: Location, to: Location): Float =
        from.distanceTo(to)

    companion object {
        private const val INTERVAL_MS = 5_000L
        private const val MIN_INTERVAL_MS = 2_000L
        private const val MAX_DELAY_MS = 10_000L
    }
}