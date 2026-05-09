//data/local/AppDatabase.kt
package com.citypulse.app.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.citypulse.app.data.local.dao.FavoriteDao
import com.citypulse.app.data.local.dao.PlaceDao
import com.citypulse.app.data.local.entities.FavoriteEntity
import com.citypulse.app.data.local.entities.PlaceEntity
/**
 *BasededonnéesRoomdeCityPulse.
 *
 *IMPORTANT—Règlesdeversionnement:
 *-Incrémenter'version'àCHAQUEmodificationduschéma.
 *-AjouteruneMigrationcorrespondantedansMIGRATIONS[].
 *-NejamaisutiliserfallbackToDestructiveMigration()enproduction.
 *-exportSchema=true:génèreunfichierJSONduschémadansschemas/(àcommiter).
 */
@Database(
    entities=[PlaceEntity::class,FavoriteEntity::class],
    version =1,
    exportSchema=true
)
@TypeConverters(Converters::class)
abstract class AppDatabase:RoomDatabase(){
//DAOs—déclarésabstract,Roomgénèrelesimplémentations
    abstract fun placeDao(): PlaceDao
    abstract fun favoriteDao():FavoriteDao
    companion object{
        @Volatile
        private var INSTANCE:AppDatabase?=null
        /**
         *Retournel'instanceuniquedelabasededonnées.
         *Thread-safegrâceàsynchronized+@Volatile.
         */
        fun getInstance(context:Context):AppDatabase{
        return INSTANCE?:synchronized(this){
            INSTANCE?:buildDatabase(context).also{INSTANCE=it}
        }
    }
        private fun buildDatabase(context:Context):AppDatabase{
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        )
//Endéveloppementuniquement—àremplacerpardesmigrationsréelles
            .fallbackToDestructiveMigration()
            .build()
    }
        private const val DATABASE_NAME="citypulse.db"
    }
}