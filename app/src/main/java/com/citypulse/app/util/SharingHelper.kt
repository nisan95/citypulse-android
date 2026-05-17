//util/SharingHelper.kt
package com.citypulse.app.util
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.citypulse.app.domain.model.Place
/**
 *UtilitairedepartagedecontenuvialesIntentsimplicitesAndroid.
 *UnIntentimplicitedecrituneACTIONsansnommerlappdestinataire.
 *LesystemeAndroidafficheunselecteuravectouteslesappscompatibles.
 *
 *ExemplesdappsquirepondentaACTION_SEND:
 * WhatsApp,Gmail,SMS,Telegram,Twitter,etc.
 */
object SharingHelper{
// ──Partagerunlieucomplet──────────────────────────────────────
    /**
     *Partagelenom, ladresse, lescoordonneesGPSetlelienGoogleMaps.
     *OuvreleselecteurdapplicationAndroid.
     */
    fun sharePlace(context:Context,place:Place){
        val text=buildShareText(place)
        shareText(context,text,"Partagerlelieuvia")
    }
// ──Partagerdutextebrut─────────────────────────────────────────
    fun shareText(
        context:Context,
        text:String,
        chooserTitle:String="Partagervia"
    ){
        val intent=Intent(Intent.ACTION_SEND).apply{
            type="text/plain"
            putExtra(Intent.EXTRA_TEXT,text)
//EXTRA_SUBJECT:utiliseparlesclientsemailcommeobjetdumail
            putExtra(Intent.EXTRA_SUBJECT,"DecouvertesurCityPulse")
        }
//createChooser:forcelaffichageduselecteurmemesiuneappestpardefaut
        context.startActivity(Intent.createChooser(intent,chooserTitle))
    }
// ──OuvrirGoogleMapsaveclescoordonneesdulieu───────────────
    fun openInMaps(context:Context,place:Place){
//FormatgeoURIstandard:geo:lat,lng?q=lat,lng(label)
        val uri=Uri.parse(
            "geo:${place.latitude},${place.longitude}"+
                    "?q=${place.latitude},${place.longitude}(${Uri.encode(place.name)})"
        )
        val intent=Intent(Intent.ACTION_VIEW,uri).apply{
//EssayerdouvrirGoogleMapsenpriorite
            setPackage("com.google.android.apps.maps")
        }
//VerifiersiGoogleMapsestinstalle
        if(intent.resolveActivity(context.packageManager)!=null){
            context.startActivity(intent)
        }else{
//Fallback:ouvrirdanslenavigateurweb
            openMapsInBrowser(context,place)
        }
    }
// ──Fallback:ouvrirMapsdanslenavigateur──────────────────────
    private fun openMapsInBrowser(context:Context,place:Place){
        val url=place.mapsUrl()
        val intent=Intent(Intent.ACTION_VIEW,Uri.parse(url))
        context.startActivity(intent)
    }
// ──Construireletextedepartage────────────────────────────────
    private fun buildShareText(place:Place):String{
        val lines=mutableListOf<String>()
        lines += "📍 ${place.name}"
        if(place.address.isNotBlank()){
            lines += "📮 ${place.address}"
        }
        lines += "🗺 ${place.category.icon} ${place.category.label}"
        lines+=""
        lines+="GPS:${place.latitude},${place.longitude}"
        lines+="VoirsurMaps:${place.mapsUrl()}"
        lines+=""
        lines+="--PartagedepuisCityPulse"
        return lines.joinToString("\n")
    }
}