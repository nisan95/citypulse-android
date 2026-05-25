// ui/map/MapFragment.kt
package com.citypulse.app.ui.map

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.citypulse.app.data.local.FusedLocationRepository
import com.citypulse.app.databinding.FragmentMapBinding
import com.citypulse.app.domain.model.Place
import com.citypulse.app.viewmodel.LocationViewModel
import com.citypulse.app.viewmodel.LocationViewModelFactory
import com.citypulse.app.viewmodel.MapViewModel
import com.citypulse.app.viewmodel.MapViewModelFactory
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.citypulse.app.R
import com.citypulse.app.ui.places.PlaceDetailFragment
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // ── ViewModels ────────────────────────────────────────────────────

    // LocationViewModel partagé avec toute l'Activity (position GPS commune)
    private val locationViewModel: LocationViewModel by activityViewModels {
        LocationViewModelFactory(FusedLocationRepository(requireContext().applicationContext))
    }

    // MapViewModel propre à cet écran (liste des lieux sur la carte)
    private val mapViewModel: MapViewModel by viewModels {
        MapViewModelFactory(
            locationViewModel,
            requireContext().applicationContext
        )
    }

    // ── État local ────────────────────────────────────────────────────

    private var googleMap: GoogleMap? = null
    private val markers = mutableMapOf<String, Marker>() // placeId → Marker
    private var selectedPlaceId: String? = null           // ✅ ID du lieu sélectionné

    // ── Cycle de vie ──────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        setupListeners()
        observeViewModels()
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationViewModel.stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Toujours nullifier pour éviter les fuites mémoire
    }

    // ── Initialisation de la carte ────────────────────────────────────

    private fun initMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) // Appelle onMapReady quand prêt
    }

    // OnMapReadyCallback — appelé quand la carte est initialisée
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        configureMap(map)

        // Déclencher le chargement des lieux une fois la carte prête
        locationViewModel.currentLocation.value?.let { loc ->
            mapViewModel.loadNearbyPlaces(loc.latitude, loc.longitude)
        }
    }

    private fun configureMap(map: GoogleMap) {
        map.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMyLocationButtonEnabled = false // On utilise notre propre FAB

            // Activer le point bleu 'Ma position' (permission déjà accordée)
            try { isMyLocationEnabled = true } catch (e: SecurityException) { /* ignoré */ }
        }

        // Clic sur l'infobulle → navigation vers le détail du lieu
        map.setOnInfoWindowClickListener { marker ->
            val place = marker.tag as? Place ?: return@setOnInfoWindowClickListener
            mapViewModel.onPlaceSelected(place)
        }

        // ✅ Clic sur un marker → afficher la card d'info
        map.setOnMarkerClickListener { marker ->
            onMarkerClicked(marker)
            true // true = on consomme l'event (empêche le zoom automatique)
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────

    private fun setupListeners() {
        // FAB : recentrer la carte sur la position actuelle
        binding.fabMyLocation.setOnClickListener {
            locationViewModel.currentLocation.value?.let { loc ->
                centerMapOnLocation(loc.latitude, loc.longitude, zoom = 15f)
            }
        }
    }

    // ── Observers ─────────────────────────────────────────────────────

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observer la position GPS → centrer et charger les lieux
                launch {
                    locationViewModel.currentLocation.collect { location ->
                        location ?: return@collect
                        centerMapOnLocation(location.latitude, location.longitude)
                        mapViewModel.onLocationUpdated(location)
                    }
                }

                // Observer l'état UI du MapViewModel
                launch {
                    mapViewModel.uiState.collect { state ->
                        renderState(state)
                    }
                }

                // Observer le mode dégradé (permission refusée)
                launch {
                    locationViewModel.isGpsAvailable.collect { available ->
                        binding.cardDegraded.isVisible = !available
                    }
                }
            }
        }
    }

    // ── Rendu de l'état UI ────────────────────────────────────────────

    private fun renderState(state: MapUiState) {
        when (state) {
            is MapUiState.Loading -> {
                binding.progressBar.isVisible = true
            }
            is MapUiState.PlacesLoaded -> {
                binding.progressBar.isVisible = false
                updateMarkers(state.places)
            }
            is MapUiState.Error -> {
                binding.progressBar.isVisible = false
                com.google.android.material.snackbar.Snackbar
                    .make(binding.root, state.message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .show()
            }
            is MapUiState.NavigateToDetail -> {
                // Navigation vers PlaceDetailFragment (implémentée Jour 7)

            }
        }
    }

    // ── Gestion des marqueurs ─────────────────────────────────────────

    private fun updateMarkers(places: List<Place>) {
        val map = googleMap ?: return

        // Supprimer les marqueurs obsolètes
        val newIds = places.map { it.id }.toSet()
        markers.entries.removeIf { (id, marker) ->
            if (id !in newIds) { marker.remove(); true } else false
        }

        // Ajouter ou mettre à jour les marqueurs
        places.forEach { place ->
            if (place.id !in markers) {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(place.latitude, place.longitude))
                        .title(place.name)
                        .snippet(
                            "${place.category.label} • " +
                                    (locationViewModel.formattedDistanceTo(place.latitude, place.longitude) ?: "")
                        )
                ) ?: return@forEach
                marker.tag = place // Stocker le Place pour y accéder au clic
                markers[place.id] = marker
            }
        }
    }

    // ── Gestion du clic sur un marker ─────────────────────────────────

    /**
     * Appelé quand l'utilisateur clique sur un marker.
     * Affiche la card d'info en bas avec le nom, la catégorie et la distance.
     *
     * ⚠️ Le tag du marker est un objet [Place] (et non un String).
     */
    private fun onMarkerClicked(marker: Marker) {
        val place = marker.tag as? Place ?: return
        selectedPlaceId = place.id

        // Remplir la card d'info
        binding.tvPlaceName.text     = place.name
        binding.tvPlaceCategory.text = "${place.category.icon} ${place.category.label}"
        binding.tvPlaceDistance.text = locationViewModel.formattedDistanceTo(
            place.latitude, place.longitude
        ) ?: "Distance inconnue"

        // Rendre la card visible
        binding.cardPlaceInfo.isVisible = true

        // Bouton "Voir le détail"
        binding.btnPlaceDetail.setOnClickListener {
                navigateToDetail(place.id)
        }

        // Centrer la carte sur le lieu sélectionné
        centerMapOnLocation(place.latitude, place.longitude, animate = true)
    }

    // ── Utilitaires carte ─────────────────────────────────────────────

    /**
     * Centre la carte sur les coordonnées données.
     * @param animate si true → animation fluide, sinon déplacement instantané
     */
    private fun centerMapOnLocation(
        lat: Double,
        lng: Double,
        zoom: Float = 14f,
        animate: Boolean = false
    ) {
        val update = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom)
        if (animate) googleMap?.animateCamera(update)
        else googleMap?.moveCamera(update)
    }

    private fun navigateToDetail(placeId: String) {
        val fragment = PlaceDetailFragment.newInstance(placeId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)  // Permet de revenir en arrière
            .commit()
    }
}