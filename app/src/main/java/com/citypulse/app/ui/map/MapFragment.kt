//ui/map/MapFragment.kt
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
import com.citypulse.app.repository.FakePlaceRepository
import com.citypulse.app.viewmodel.LocationViewModel
import com.citypulse.app.viewmodel.LocationViewModelFactory
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.citypulse.app.viewmodel.MapViewModelFactory
import com.citypulse.app.viewmodel.MapViewModel
import kotlinx.coroutines.launch
class MapFragment:Fragment(),OnMapReadyCallback{
    private var _binding:FragmentMapBinding?=null
    private val binding get()=_binding!!
//LocationViewModelpartagéavectoutel'Activity(positionGPScommune)
    private val locationViewModel:LocationViewModel by activityViewModels{
        LocationViewModelFactory(FusedLocationRepository(requireContext().applicationContext))
    }
//MapViewModelpropreàcetécran(listedeslieuxsurlacarte)
    private val mapViewModel:MapViewModel by viewModels{
        MapViewModelFactory(FakePlaceRepository())
    }
    private var googleMap:GoogleMap?=null
    private val markers=mutableMapOf<String,Marker>() //placeId→Marker
    override fun onCreateView(inflater:LayoutInflater,container:ViewGroup?,state:Bundle?):View{
        _binding=FragmentMapBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view:View,savedInstanceState:Bundle?){
        super.onViewCreated(view,savedInstanceState)
        initMap()
        setupListeners()
        observeViewModels()
    }
// ── Initialisationdelacarte ─────────────────────────────────────
    private fun initMap(){
        val mapFragment=childFragmentManager
            .findFragmentById(com.citypulse.app.R.id.map)as SupportMapFragment
                mapFragment.getMapAsync(this) //AppelleonMapReadyquandprêt
    }
//OnMapReadyCallback—appeléquandlacarteestinitialisée
    override fun onMapReady(map:GoogleMap){
        googleMap=map
        configureMap(map)
//Déclencherlechargementdeslieuxunefoislacarteprête
        locationViewModel.currentLocation.value?.let{loc->
            mapViewModel.loadNearbyPlaces(loc.latitude, loc.longitude)
        }
    }
    private fun configureMap(map:GoogleMap){
        map.apply{
            uiSettings.isZoomControlsEnabled =true
            uiSettings.isCompassEnabled =true
            uiSettings.isMyLocationButtonEnabled=false //OnutilisenotrepropreFAB
//Activerlepointbleu'Maposition'(permissiondéjàaccordée)
            try{isMyLocationEnabled=true}catch(e:SecurityException){/*ignoré*/}
        }
//Clicsuruneinfobulle → naviguerversledétaildulieu
        map.setOnInfoWindowClickListener{marker->
            val place=marker.tag as?Place?:return@setOnInfoWindowClickListener
            mapViewModel.onPlaceSelected(place)
        }
    }
// ── Listeners ─────────────────────────────────────────────────────
    private fun setupListeners(){
//FAB:recentrerlacartesurlapositionactuelle
        binding.fabMyLocation.setOnClickListener{
            locationViewModel.currentLocation.value?.let{loc->
                centerMap(loc.latitude, loc.longitude,zoom=15f)
            }
        }
    }
// ── Observers ─────────────────────────────────────────────────────
    private fun observeViewModels(){
        viewLifecycleOwner.lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
//ObserverlapositionGPS → centreretchargerleslieux
                launch{
                    locationViewModel.currentLocation.collect{location->
                        location?:return@collect
                        centerMap(location.latitude, location.longitude)
                        mapViewModel.onLocationUpdated(location)
                    }
                }
//Observerl'étatUIduMapViewModel
                launch{
                    mapViewModel.uiState.collect{state->
                        renderState(state)
                    }
                }
//Observerlemodedégradé(permissionrefusée)
                launch{
                    locationViewModel.isGpsAvailable.collect{available->
                        binding.cardDegraded.isVisible=!available
                    }
                }
            }
        }
    }
// ── Rendudel'étatUI ─────────────────────────────────────────────
    private fun renderState(state:MapUiState){
        when(state){
            is MapUiState.Loading->{
                binding.progressBar.isVisible=true
            }
            is MapUiState.PlacesLoaded->{
                binding.progressBar.isVisible=false
                updateMarkers(state.places)
            }
            is MapUiState.Error->{
                binding.progressBar.isVisible=false
//Snackbard'erreur
                com.google.android.material.snackbar.Snackbar
                    .make(binding.root,state.message,com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .show()
            }
            is MapUiState.NavigateToDetail->{
//NavigationversPlaceDetailFragment(implémentéeJour7)
            }
        }
    }
// ── Gestiondesmarqueurs ──────────────────────────────────────────
    private fun updateMarkers(places:List<Place>){
        val map=googleMap?:return
//Supprimerlesmarqueursobsolètes
        val newIds=places.map{it.id}.toSet()
        markers.entries.removeIf{(id,marker)->
            if(id!in newIds){marker.remove();true}else false
        }
//Ajouteroumettreàjourlesmarqueurs
        places.forEach{place->
            if(place.id!in markers){
            val marker=map.addMarker(
                MarkerOptions()
                    .position(LatLng(place.latitude,place.longitude))
                    .title(place.name)
                    .snippet(
                        "${place.category.label} • "+
                                (locationViewModel.formattedDistanceTo(place.latitude,place.longitude)?:"")
                    )
            )?:return@forEach
            marker.tag=place //StockerlePlacepouryaccéderauclic
            markers[place.id]=marker
        }
        }
    }
// ── Utilitairescarte ──────────────────────────────────────────────
    private fun centerMap(lat:Double, lng:Double,zoom:Float=14f){
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng),zoom)
        )
    }
    override fun onResume(){
        super.onResume()
        locationViewModel.startLocationUpdates()
    }
    override fun onPause(){
        super.onPause()
        locationViewModel.stopLocationUpdates()
    }
    override fun onDestroyView(){
        super.onDestroyView()
        _binding=null //Toujoursnullifierpouréviterlesfuitesmémoire
    }
}