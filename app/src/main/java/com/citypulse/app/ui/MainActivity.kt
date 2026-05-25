package com.citypulse.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.citypulse.app.R
import com.citypulse.app.databinding.ActivityMainBinding
import com.citypulse.app.ui.favorites.FavoritesFragment
import com.citypulse.app.ui.map.MapFragment
import com.citypulse.app.ui.places.PlaceListFragment
import com.citypulse.app.ui.places.PlaceDetailFragment
import com.citypulse.app.util.PermissionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)

        // Charger la carte au démarrage (seulement si pas de rotation)
        if (savedInstanceState == null) {
            loadFragment(MapFragment())
        }

        // Configurer la navigation du bas
        setupBottomNavigation()

        // Demander les permissions GPS au démarrage
        // ✅ On appelle directement requestLocationPermissions() sans hasLocationPermissions()
        requestLocationPermissions(
            onGranted = {
                com.citypulse.app.service.LocationServiceManager.start(this)
            }
        )


    }

    // ── Navigation ────────────────────────────────────────────────────

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map      -> { loadFragment(MapFragment()); true }
                R.id.nav_list     -> { loadFragment(PlaceListFragment()); true }
                R.id.nav_favorites-> { loadFragment(FavoritesFragment()); true }
                else              -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
    }

    /**
     * Navigation vers PlaceDetailFragment depuis MapFragment.
     * Utilise addToBackStack pour permettre le retour arrière.
     */
    fun navigateToPlaceDetail(placeId: String) {
        val fragment = PlaceDetailFragment.newInstance(placeId)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null) // ✅ Bouton Back revient à la carte
        }
    }

    // ── Permissions ───────────────────────────────────────────────────

    fun requestLocationPermissions(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        permissionManager.requestLocationPermissions(
            onGranted = {
                com.citypulse.app.service.LocationServiceManager.start(this)
                onGranted()
            },
            onDenied = {
                Toast.makeText(
                    this,
                    "Mode dégradé : carte désactivée sans localisation.",
                    Toast.LENGTH_LONG
                ).show()
                onDenied()
            }
        )
    }

    fun requestNotificationPermission(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        permissionManager.requestNotificationPermission(onGranted, onDenied)
    }

    // ── Cycle de vie ──────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()
        // Arrêter le service quand l'Activity est détruite
        com.citypulse.app.service.LocationServiceManager.stop(this)
    }
}