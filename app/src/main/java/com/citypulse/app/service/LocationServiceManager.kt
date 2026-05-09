// service/LocationServiceManager.kt
package com.citypulse.app.service
import android.content.Context
import android.content.Intent
import android.os.Build
/**
 * Helper centralisé pour gérer le cycle de vie de LocationForegroundService.
 *
 * Utilisation :
 * LocationServiceManager.start(context) // Dans onResume ou après permission accordée
 * LocationServiceManager.stop(context) // Dans onDestroy ou déconnexion
 */
object LocationServiceManager {
    fun start(context: Context) {
        val intent = Intent(context, LocationForegroundService::class.java)
// Sur Android 8+, startForegroundService() est obligatoire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    fun stop(context: Context) {
        context.stopService(Intent(context, LocationForegroundService::class.java))
    }
}