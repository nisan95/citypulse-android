package com.citypulse.app.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.citypulse.app.data.local.AppDatabase
import com.citypulse.app.data.local.entities.PlaceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaceDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PlaceDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.placeDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun insertAndRetrievePlace() = runTest {
        val place = PlaceEntity(
            id = "test_1",
            name = "Musée du Louvre",
            address = "Rue de Rivoli, Paris",
            latitude = 48.8606,
            longitude = 2.3376,
            category = "MUSEUM",
            photoUrl = null,
            rating = null,
            description = null,
            phoneNumber = null,
            website = null
        )
        dao.insertPlace(place)
        val retrieved = dao.getPlaceById("test_1")
        assertNotNull(retrieved)
        assertEquals("Musée du Louvre", retrieved?.name)
    }

    @Test
    fun searchPlacesByName() = runTest {
        dao.insertPlaces(listOf(
            PlaceEntity(id="1", name="Café de Flore", address="",
                latitude=48.85, longitude=2.33, category="CAFE",
                photoUrl=null, rating=null, description=null,
                phoneNumber=null, website=null),
            PlaceEntity(id="2", name="Jardin du Luxembourg", address="",
                latitude=48.84, longitude=2.33, category="PARK",
                photoUrl=null, rating=null, description=null,
                phoneNumber=null, website=null),
        ))
        val results = dao.searchPlaces("Café").first()
        assertEquals(1, results.size)
        assertEquals("Café de Flore", results[0].name)
    }

    @Test
    fun deleteCacheBeforeTimestamp() = runTest {
        val oldPlace = PlaceEntity(
            id = "old", name = "Vieux lieu", address = "",
            latitude = 0.0, longitude = 0.0,
            category = "OTHER", photoUrl = null,
            rating = null, description = null,
            phoneNumber = null, website = null,
            cachedAt = System.currentTimeMillis() - 7_200_000
        )
        dao.insertPlace(oldPlace)
        val deleted = dao.deletePlacesCachedBefore(
            System.currentTimeMillis() - 3_600_000
        )
        assertEquals(1, deleted)
        assertNull(dao.getPlaceById("old"))
    }
}