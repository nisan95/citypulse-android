//data/remote/NetworkConstants.kt
package com.citypulse.app.data.remote
import com.citypulse.app.BuildConfig
/**
 *TouteslesconstantesréseaudeCityPulse.
 *Lesvaleurssensibles(clésAPI)viennentdeBuildConfig,
 *lui-mêmealimentéparlocal.properties(jamaiscommité).
 */
object NetworkConstants{
//URL de base de l'API(définie dans local.properties → BuildConfig)
    val BASE_URL:String get()=BuildConfig.BASE_URL
//CléAPI(définie dans local.properties → BuildConfig)
    val API_KEY:String get()=BuildConfig.API_KEY
//Timeouts
    const val CONNECT_TIMEOUT_SEC=30L
    const val READ_TIMEOUT_SEC =30L
    const val WRITE_TIMEOUT_SEC =30L
//CacheHTTPsurdisque
    const val CACHE_SIZE_BYTES =10L*1024L*1024L //10Mo
//Pagination
    const val DEFAULT_PAGE_SIZE =20
    const val DEFAULT_RADIUS_M =1000 //1kmpardéfaut
//Headers
    const val HEADER_CONTENT_TYPE="Content-Type"
    const val HEADER_ACCEPT ="Accept"
    const val HEADER_API_KEY ="X-API-Key"
    const val MIME_JSON ="application/json"
}