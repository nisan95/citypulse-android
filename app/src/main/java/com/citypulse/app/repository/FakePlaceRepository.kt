package com.citypulse.app.repository

import com.citypulse.app.domain.model.Category
import com.citypulse.app.domain.model.Favorite
import com.citypulse.app.domain.model.Place
import com.citypulse.app.domain.repository.PlaceRepository
import com.citypulse.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePlaceRepository : PlaceRepository {

    private val fakePlaces = listOf(
        Place(id="1", name="Musée du Louvre", address="Rue de Rivoli, Paris",
            latitude=48.8606, longitude=2.3376, category=Category.MUSEUM),
        Place(id="2", name="Tour Eiffel", address="Champ de Mars, Paris",
            latitude=48.8584, longitude=2.2945, category=Category.OTHER),
        Place(id="3", name="Café de Flore", address="172 Bd Saint-Germain, Paris",
            latitude=48.8540, longitude=2.3326, category=Category.CAFE),
        Place(id="4", name="Jardin du Luxembourg", address="Rue de Médicis, Paris",
            latitude=48.8462, longitude=2.3372, category=Category.PARK),
        Place(id="5", name="Le Marais", address="Rue des Rosiers, Paris",
            latitude=48.8572, longitude=2.3538, category=Category.SHOP),
        Place(id="6", name="Brasserie Lipp", address="151 Bd Saint-Germain, Paris",
            latitude=48.8537, longitude=2.3322, category=Category.RESTAURANT),
        Place(id="7", name="Centre Pompidou", address="Place Georges-Pompidou, Paris",
            latitude=48.8606, longitude=2.3522, category=Category.MUSEUM),
        Place(id="8", name="Palais Royal", address="Pl. du Palais Royal, Paris",
            latitude=48.8638, longitude=2.3370, category=Category.OTHER),
    )

    override suspend fun getNearbyPlaces(
        latitude: Double, longitude: Double, radiusMeters: Int
    ): Result<List<Place>> = Result.Success(fakePlaces)

    override suspend fun searchPlaces(query: String): Result<List<Place>> =
        Result.Success(fakePlaces.filter {
            it.name.contains(query, ignoreCase = true)
        })

    override suspend fun getPlaceById(id: String): Result<Place> {
        val place = fakePlaces.firstOrNull { it.id == id }
        return if (place != null) Result.Success(place)
        else Result.Error("Lieu introuvable")
    }

    override fun getCachedPlaces(): Flow<List<Place>> = flowOf(fakePlaces)

    override suspend fun cachePlaces(places: List<Place>) { /* no-op en fake */ }
}