// service/LocationForegroundService.kt
package com.citypulse.app.service

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.citypulse.app.CityPulseApplication
import com.citypulse.app.R
import com.citypulse.app.data.local.FusedLocationRepository
import com.citypulse.app.ui.MainActivity
import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.data.local.AppDatabase
import com.citypulse.app.data.local.toDomain
import com.citypulse.app.service.ProximityChecker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class LocationForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationRepository: FusedLocationRepository

    // AJOUT POU FAVORIS & PROXIMITE
    private lateinit var proximityChecker: ProximityChecker
    private var cachedFavorites: List<Favorite> = emptyList()
    private var favoritesJob: Job? = null
    private var locationJob: Job? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.citypulse.app.STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        locationRepository = FusedLocationRepository(applicationContext)

        // Inisyalize chèk pwoksimite a
        proximityChecker = ProximityChecker(applicationContext)

        // Kòmanse koute chanjman nan baz de done favoris yo
        startObservingFavorites()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification("Initialisation..."))
        startLocationTracking()

        return START_STICKY
    }

    // ── Observe Favoris yo depi Room ──────────────────────────────────
    private fun startObservingFavorites() {
        val db = AppDatabase.getInstance(applicationContext)
        val favoriteDao = db.favoriteDao()

        favoritesJob = serviceScope.launch {
            // Nou kolekte favoris yo chak fwa yo chanje nan DB
            favoriteDao.getFavoritePlaces().collect { placeEntities ->
                cachedFavorites = placeEntities.map { entity ->
                    val place = entity.toDomain()
                    Favorite(place = place)
                }
            }
        }
    }

    // ── Suivi GPS ──────────────────────────────────────────────────────
    private fun startLocationTracking() {
        locationJob = serviceScope.launch {
            locationRepository.locationUpdates()
                .catch { e ->
                    updateNotification("GPS indisponible : ${e.message}")
                }
                .collect { location ->
                    onNewLocation(location)
                }
        }
    }

    // Lè yon nouvo pozisyon rive
    private fun onNewLocation(location: Location) {
        // 1. Mete notifikasyon an ajou
        updateNotification(
            "Lat: %.4f, Lng: %.4f".format(location.latitude, location.longitude)
        )

        // 2. Tcheke si nou pre yon kote ki nan favoris yo
        if (cachedFavorites.isNotEmpty()) {
            proximityChecker.checkProximity(location, cachedFavorites)
        }
    }

    // ── Construction de la notification ────────────────────────────────
    private fun buildNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, LocationForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CityPulseApplication.CHANNEL_LOCATION)
            .setContentTitle("CityPulse — Suivi actif")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Arrêter",
                stopIntent
            )
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    // ── Cycle de vie du Service ────────────────────────────────────────
    override fun onDestroy() {
        super.onDestroy()
        favoritesJob?.cancel()
        locationJob?.cancel()
        serviceScope.cancel() // Netwaye tout kòroutine yo nèt
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}