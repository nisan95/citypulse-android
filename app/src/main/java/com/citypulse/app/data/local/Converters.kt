//data/local/Converters.kt
package com.citypulse.app.data.local
import androidx.room.TypeConverter
import com.citypulse.app.domain.model.Category
import java.util.Date
/**
 *Type Converters pour Room—sérialise les types non primitifs.
 *Déclarédans@Databasevia@TypeConverters(Converters::class).
 */
class Converters{
// ──Date↔Long(timestamp)───────────────────────────────────────
    @TypeConverter
    fun fromTimestamp(value:Long?):Date?=value?.let{Date(it)}
    @TypeConverter
    fun dateToTimestamp(date:Date?):Long?=date?.time
// ──Category(enum)↔String──────────────────────────────────────
    @TypeConverter
    fun fromCategory(category:Category?):String?=category?.name
    @TypeConverter
    fun toCategory(value:String?):Category?=
    value?.let{Category.fromString(it)}
}