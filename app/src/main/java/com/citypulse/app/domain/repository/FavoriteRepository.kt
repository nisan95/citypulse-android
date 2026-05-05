//app/src/main/java/com/citypulse/app/domain/repository/FavoriteRepository.kt
package com.citypulse.app.domain.repository
import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.Flow
/**
 *Interfacedegestiondesfavoris.
 *ToujoursliredepuisRoom—pasderéseaupourlesfavoris.
 */
interface FavoriteRepository{
    /**Fluxcontinudetouslesfavoris—semetàjourautomatiquement*/
    fun getAllFavorites():Flow<List<Favorite>>
    /**Vérifiesiunlieuestenfavori—retourneunFlowobservable*/
    fun isFavorite(placeId:String):Flow<Boolean>
    /**Ajouteunlieuauxfavoris*/
    suspend fun addFavorite(favorite:Favorite):Result<Unit>
    /**Supprimeunfavoriparl'IDdulieu*/
    suspend fun removeFavorite(placeId:String):Result<Unit>
    /**Metàjourlanotepersonnelled'unfavori*/
    suspend fun updateNote(placeId:String,note:String):Result<Unit>
}