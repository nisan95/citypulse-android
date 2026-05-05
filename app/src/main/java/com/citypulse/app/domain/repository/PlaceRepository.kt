//app/src/main/java/com/citypulse/app/domain/repository/PlaceRepository.kt
package com.citypulse.app.domain.repository
import com.citypulse.app.domain.model.Place
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.Flow
/**
 *Interfaceuniquedevéritépourleslieuxd'intérêt.
 *P2etP3implémententcetteinterfacechacundeleurcôté.
 */
interface PlaceRepository{
    /**Récupèreleslieuxàproximité(APIoucacheRoomselondisponibilitéréseau)*/
    suspend fun getNearbyPlaces(
        latitude:Double,
        longitude:Double,
        radiusMeters:Int=1000
    ):Result<List<Place>>
    /**Recherchetextuelledelieux*/
    suspend fun searchPlaces(query:String):Result<List<Place>>
    /**Récupèreledétaild'unlieuparsonidentifiant*/
    suspend fun getPlaceById(id:String):Result<Place>
    /**Fluxcontinudelieuxmisencachelocalement(Room)*/
    fun getCachedPlaces():Flow<List<Place>>
    /**SauvegardeunelistedelieuxdanslecacheRoom*/
    suspend fun cachePlaces(places:List<Place>)
}