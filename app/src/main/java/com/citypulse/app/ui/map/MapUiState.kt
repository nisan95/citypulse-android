//ui/map/MapUiState.kt
package com.citypulse.app.ui.map
import com.citypulse.app.domain.model.Place
sealed class MapUiState{
    object Loading :MapUiState() //Chargementlieux
    data class PlacesLoaded(val places:List<Place>):MapUiState() //Marqueursàafficher
    data class Error(val message:String) :MapUiState() //Erreurréseau
    data class NavigateToDetail(val place:Place):MapUiState()//Navigationdétail
}