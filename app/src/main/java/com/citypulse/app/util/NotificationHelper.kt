package com.citypulse.app.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.citypulse.app.CityPulseApplication
import com.citypulse.app.domain.model.Place
import com.citypulse.app.ui.MainActivity

object NotificationHelper {

    private const val PROXIMITY_NOTIFICATION_BASE_ID = 2000

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendProximityNotification(
        context: Context,
        place: Place,
        distance: String
    ) {
        if (!hasNotificationPermission(context)) return

        val pendingIntent = buildMainActivityPendingIntent(context, place.id)

        val notification = NotificationCompat.Builder(
            context,
            CityPulseApplication.CHANNEL_PROXIMITY
        )
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Lieu favori proche !")
            .setContentText("${place.name} est à $distance")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Votre favori '${place.name}' est à $distance.\n" +
                                "${place.category.icon} ${place.category.label}"
                    )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationId = PROXIMITY_NOTIFICATION_BASE_ID + place.id.hashCode()
        getNotificationManager(context).notify(notificationId, notification)
    }

    fun cancelProximityNotification(context: Context, placeId: String) {
        val notificationId = PROXIMITY_NOTIFICATION_BASE_ID + placeId.hashCode()
        getNotificationManager(context).cancel(notificationId)
    }

    fun cancelAllNotifications(context: Context) {
        getNotificationManager(context).cancelAll()
    }

    private fun buildMainActivityPendingIntent(
        context: Context,
        placeId: String
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("place_id", placeId)
        }
        return PendingIntent.getActivity(
            context,
            placeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}