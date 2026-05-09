//data/remote/AuthInterceptor.kt
package com.citypulse.app.data.remote
import okhttp3.Interceptor
import okhttp3.Response
/**
 *IntercepteurOkHttpquiajouteautomatiquement:
 *-LeheaderContent-Type
 *-LeheaderAccept
 *-LacléAPI(X-API-Keyouenqueryparamselonl'APIchoisie)
 *
 *InjectédansOkHttpClient.Builder().addInterceptor(AuthInterceptor())
 */
class AuthInterceptor:Interceptor{
    override fun intercept(chain:Interceptor.Chain):Response{
        val originalRequest=chain.request()
//StratégieA:cléAPIdansunheaderHTTP(OpenTripMap,Foursquare...)
        val requestWithHeaders=originalRequest.newBuilder()
            .addHeader(NetworkConstants.HEADER_CONTENT_TYPE,NetworkConstants.MIME_JSON)
            .addHeader(NetworkConstants.HEADER_ACCEPT, NetworkConstants.MIME_JSON)
            .addHeader(NetworkConstants.HEADER_API_KEY, NetworkConstants.API_KEY)
            .build()
//StratégieB:cléAPIenqueryparameter(alternativepourcertainesAPIs)
//valurlWithKey=originalRequest.url.newBuilder()
// .addQueryParameter("apikey",NetworkConstants.API_KEY)
// .build()
//valrequestWithKey=originalRequest.newBuilder().url(urlWithKey).build()
        return chain.proceed(requestWithHeaders)
    }
}