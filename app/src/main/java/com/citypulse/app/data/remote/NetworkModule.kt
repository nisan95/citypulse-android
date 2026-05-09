//data/remote/NetworkModule.kt
package com.citypulse.app.data.remote
import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
/**
 *Singletondeconfigurationréseau.
 *CréeuneseuleinstancedeRetrofitetOkHttpClientpourtoutel'app.
 *
 *UtilisationdepuisunRepository:
 * valapiService=NetworkModule.provideApiService(context)
 */
object NetworkModule{
//InstanceuniquedeRetrofit(lazy=crééeseulementaupremierappel)
    @Volatile private var retrofitInstance:Retrofit?=null
// ──OkHttpClient─────────────────────────────────────────────────
    fun provideOkHttpClient(context:Context, isDebug:Boolean=false):OkHttpClient{
        val cacheDir =File(context.cacheDir,"http_cache")
        val httpCache=Cache(cacheDir,NetworkConstants.CACHE_SIZE_BYTES)
        return OkHttpClient.Builder()
//Timeouts
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SEC,TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
//Authentificationautomatiquesurtouteslesrequêtes
            .addInterceptor(AuthInterceptor())
//CacheHTTPsurdisque(10Mo)
            .cache(httpCache)
//Intercepteurdecacheoffline:retournerlecachesipasderéseau
            .addInterceptor(OfflineCacheInterceptor(context))
//Logsréseau—SEULEMENTenmodedebug
            .apply{
                if(isDebug){
                    addInterceptor(HttpLoggingInterceptor().apply{
                        level=HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }
// ──Retrofit─────────────────────────────────────────────────────
    fun provideRetrofit(context:Context, isDebug:Boolean=false):Retrofit{
        return retrofitInstance?:synchronized(this){
            Retrofit.Builder()
                .baseUrl(NetworkConstants.BASE_URL
                    .takeIf{it.isNotBlank()}
                    ?:"https://api.opentripmap.com/0.1/" //Fallbacksinonconfiguré
                )
                .client(provideOkHttpClient(context, isDebug))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also{retrofitInstance=it}
        }
    }
// ──CréerunserviceRetrofittype-safe───────────────────────────
    inline fun<reified T>provideService(context:Context, isDebug:Boolean=false):T{
        return provideRetrofit(context, isDebug).create(T::class.java)
    }
// ──RaccourcipourApiService─────────────────────────────────────
    fun provideApiService(context:Context, isDebug:Boolean=false):ApiService{
        return provideService(context, isDebug)
    }
}