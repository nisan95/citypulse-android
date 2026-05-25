
package com.citypulse.app
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

/**
 * Classe Application de CityPulse. * Point d'entrée global de l'app — initialisations singleton ici. * Déclarée dans AndroidManifest.xml par P3 (android:name). */
class CityPulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    private fun createNotificationChannels() {
// ── Canal de localisation (ForegroundService) ──────────────
        val locationChannel = NotificationChannel(
            CHANNEL_LOCATION, "Suivi de position", NotificationManager.IMPORTANCE_LOW // Silencieux
        ).apply {
            description = "Notification du service de localisation en arrière-plan" }
// ── Canal de proximité (alertes 500 m) ─────────────────────
        val proximityChannel = NotificationChannel(
            CHANNEL_PROXIMITY, "Lieux à proximité", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alertes quand un lieu d'intérêt est proche" }
        getSystemService(NotificationManager::class.java).apply {
            createNotificationChannel(locationChannel)
            createNotificationChannel(proximityChannel)
        }
    }
    companion object {
        const val CHANNEL_LOCATION = "channel_location"
        const val CHANNEL_PROXIMITY = "channel_proximity" }
}
