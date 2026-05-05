//app/src/main/java/com/citypulse/app/util/Result.kt
package com.citypulse.app.util
/**
 *Wrappergénériquepourlesrésultatsd'opérationsasynchrones.
 *UtilisédansTOUTleprojet—nejamaisretournernulloulancerd'exception
 *directementdepuisunRepositoryouUseCase.
 *
 *Usage:
 * when(result){
 * isResult.Success->afficherresult.data
 * isResult.Error->afficherresult.message
 * isResult.Loading->afficherlespinner
 * }
 */
sealed class Result<out T>{
    /**Opérationréussie—contientlesdonnées*/
    data class Success<T>(val data:T):Result<T>()
    /**Erreur—contientlemessagelisibleetl'exceptionoptionnelle*/
    data class Error(
        val message:String,
        val exception:Exception?=null
    ):Result<Nothing>()
    /**Chargementencours*/
    object Loading:Result<Nothing>()
}
//──Extensionsutilitaires─────────────────────────────────────────────
/**ExécuterunblocseulementsiSuccess*/
inline fun<T>Result<T>.onSuccess(action:(T)->Unit):Result<T>{
    if(this is Result.Success)action(data)
    return this
}
/**ExécuterunblocseulementsiError*/
inline fun<T>Result<T>.onError(action:(String,Exception?)->Unit):Result<T>{
    if(this is Result.Error)action(message,exception)
    return this
}