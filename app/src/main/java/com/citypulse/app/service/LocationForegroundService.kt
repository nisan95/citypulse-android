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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
/**
 * Service de localisation en arrière-plan.
 * Tourne tant que l'utilisateur ne l'arrête pas explicitement.
 *
 * Démarrage : depuis MainActivity ou un BroadcastReceiver.
 * startForegroundService(Intent(context, LocationForegroundService::class.java))
 *
 * Arrêt :
 * stopService(Intent(context, LocationForegroundService::class.java))
 */
class LocationForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationRepository: FusedLocationRepository
    private var lastLocation: Location? = null
    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.citypulse.app.STOP_SERVICE"
    }
    override fun onCreate() {
        super.onCreate()
        locationRepository = FusedLocationRepository(applicationContext)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
// Gérer l'action d'arrêt depuis la notification
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
// Passer immédiatement en foreground avec la notification
        startForeground(NOTIFICATION_ID, buildNotification("Initialisation..."))
// Démarrer le suivi GPS dans une coroutine
        startLocationTracking()
// START_STICKY : Android redémarre le service s'il est tué (ex: manque mémoire)
        return START_STICKY
    }
    // ── Suivi GPS ──────────────────────────────────────────────────────
    private fun startLocationTracking() {
        serviceScope.launch {
            locationRepository.locationUpdates()
                .catch { e ->
                    updateNotification("GPS indisponible : ${e.message}")
                }
                .collect { location ->
                    lastLocation = location
// Mettre à jour la notification avec les coordonnées
                    updateNotification(
                        "Lat: %.4f, Lng: %.4f".format(location.latitude, location.longitude)
                    )
// Vérifier la proximité des lieux (implémenté Jour 8 par P3)
// checkProximity(location)
                }
        }
    }
    // ── Construction de la notification ────────────────────────────────
    private fun buildNotification(contentText: String): Notification {
// Intent pour ouvrir l'app au clic sur la notification
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
// Intent pour arrêter le service depuis la notification
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
            .setOngoing(true) // Non-dismissible par l'utilisateur
            .setPriority(NotificationCompat.PRIORITY_LOW) // Silencieux
            .addAction( // Bouton 'Arrêter' dans la notification
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
        serviceScope.cancel() // Annuler toutes les coroutines
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    // Service non bindé (pas d'interface AIDL)
    override fun onBind(intent: Intent?): IBinder? = null
}