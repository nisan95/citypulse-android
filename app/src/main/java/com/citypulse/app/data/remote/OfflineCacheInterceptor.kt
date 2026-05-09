//data/remote/OfflineCacheInterceptor.kt
package com.citypulse.app.data.remote
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
/**
 *Intercepteurquidétectel'absencederéseauetforceOkHttp
 *àretournerlaréponseencacheplutôtquedelanceruneexception.
 *
 *Enmodeconnecté :requêtenormale,réponsemiseencache.
 *Enmodehors-ligne:retournelecache(max7jours).
 */
class OfflineCacheInterceptor(private val context:Context):Interceptor{
    override fun intercept(chain:Interceptor.Chain):Response{
        var request=chain.request()
        if(!isNetworkAvailable()){
//Pasderéseau → forcerl'utilisationducache
            request=request.newBuilder()
                .cacheControl(
                    CacheControl.Builder()
                        .onlyIfCached() //N'utiliserquelecache
                        .maxStale(7,TimeUnit.DAYS) //Accepteruncachede7jours
                        .build()
                )
                .build()
        }
        return chain.proceed(request)
    }
    private fun isNetworkAvailable():Boolean{
        val cm=context.getSystemService(Context.CONNECTIVITY_SERVICE)as ConnectivityManager
        val network=cm.activeNetwork?:return false
        val caps =cm.getNetworkCapabilities(network)?:return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}