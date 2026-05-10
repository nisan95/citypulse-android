package com.citypulse.app.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.citypulse.app.databinding.ActivityMainBinding
import com.citypulse.app.util.PermissionManager
import com.citypulse.app.util.Result

import com.citypulse.app.data.remote.NetworkModule

import com.citypulse.app.data.remote.PlaceApiRepository

import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)

        // 1. Demander la localisation au démarrage
        requestLocationPermissions()


    }

    fun requestLocationPermissions(onGranted: () -> Unit = {}, onDenied: () -> Unit = {}) {
        permissionManager.requestLocationPermissions(
            onGranted = {
                // Démarrer le service de localisation en arrière-plan
                com.citypulse.app.service.LocationServiceManager.start(this)
                onGranted()
            },
            onDenied = {
                Toast.makeText(this, "Mode dégradé.", Toast.LENGTH_LONG).show()
                onDenied()
            }
        )
    }

    fun requestNotificationPermission(onGranted: () -> Unit = {}, onDenied: () -> Unit = {}) {
        permissionManager.requestNotificationPermission(onGranted, onDenied)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrêter le service quand l'Activity est détruite
        com.citypulse.app.service.LocationServiceManager.stop(this)
    }
}