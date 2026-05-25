package com.citypulse.app.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionManager(private val activity: ComponentActivity) {
    private var onGranted: (() -> Unit)? = null
    private var onDenied: (() -> Unit)? = null
    // Launcher multi-permissions — doit être enregistré AVANT onCreate()
    private val multiLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            if (results.values.all { it }) onGranted?.invoke()
            else onDenied?.invoke()
        }
    // Launcher permission unique (notification)
    private val singleLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) onGranted?.invoke() else onDenied?.invoke()
        }
    // ── API publique ──────────────────────────────────────────────────
    fun requestLocationPermissions(onGranted: () -> Unit, onDenied: () -> Unit = {}) {
        requestPermissions(
            permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            rationaleTitle = "Localisation requise",
            rationaleMessage = "CityPulse a besoin de votre position pour afficher " +
                    "les lieux d'intérêt autour de vous et calculer les distances.",
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    fun requestNotificationPermission(onGranted: () -> Unit, onDenied: () -> Unit = {}) {
// Android 12 et inférieur : permission automatique, pas besoin de demander
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onGranted()
            return
        }
        this.onGranted = onGranted
        this.onDenied = onDenied
        val perm = Manifest.permission.POST_NOTIFICATIONS
        when {
            isGranted(perm) -> onGranted()
            activity.shouldShowRequestPermissionRationale(perm) ->
                showDialog("Notifications",
                    "Activez les notifications pour être alerté des lieux proches.",
                    onConfirm = { singleLauncher.launch(perm) },
                    onCancel = onDenied
                )
            else -> singleLauncher.launch(perm)
        }
    }
// ── Méthode générique interne ──────────────────────────────────────
private fun requestPermissions(
    permissions: Array<String>,
    rationaleTitle: String,
    rationaleMessage: String,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    this.onGranted = onGranted
    this.onDenied  = onDenied

    val missing = permissions.filter { !isGranted(it) }

    when {
        // Toutes accordées
        missing.isEmpty() -> onGranted()

        // Au moins une avec rationale -> expliquer avant de demander
        missing.any { activity.shouldShowRequestPermissionRationale(it) } -> {
            showDialog(
                rationaleTitle,
                rationaleMessage,
                onConfirm = { multiLauncher.launch(missing.toTypedArray()) },
                onCancel  = onDenied
            )
        }

        // Vérifier si déjà refusé définitivement
        // shouldShowRequestPermissionRationale == false ET déjà demandé
        // = refus définitif -> envoyer dans les paramètres
        missing.all { perm ->
            val alreadyRequested = activity
                .getSharedPreferences("perm_prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("asked_$perm", false)
            !activity.shouldShowRequestPermissionRationale(perm) && alreadyRequested
        } -> {
            // Refus définitif détecté -> ouvrir les paramètres
            showSettingsDialog()
        }

        // Première demande -> marquer comme demandé et lancer
        else -> {
            missing.forEach { perm ->
                activity.getSharedPreferences("perm_prefs",
                    android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("asked_$perm", true)
                    .apply()
            }
            multiLauncher.launch(missing.toTypedArray())
        }
    }
}

    private fun isGranted(perm: String) =
        ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED

    private fun showDialog(title: String, msg: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(title).setMessage(msg)
            .setPositiveButton("Accorder") { _, _ -> onConfirm() }
            .setNegativeButton("Annuler") { _, _ -> onCancel() }
            .setCancelable(false).show()
    }

    private fun showSettingsDialog() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Permission refusée")
            .setMessage("Activez la permission dans les Paramètres de l'application.")
            .setPositiveButton("Paramètres") { _, _ ->
                activity.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                )
            }
            .setNegativeButton("Annuler") { _, _ -> onDenied?.invoke() }
            .show()
    }
}