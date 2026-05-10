//ui/places/PlaceAdapter.kt
package com.citypulse.app.ui.places
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.citypulse.app.databinding.ItemPlaceBinding
import com.citypulse.app.domain.model.Place
/**
 *AdaptateurRecyclerViewpourlalistedeslieux.
 *HeritedeListAdapter(DiffUtil integre)--plusefficacequeRecyclerView.Adapter.
 *
 *@paramonItemClick Appeléquandl'utilisateurcliquesurunitem
 *@paramonFavoriteClickAppeléquandl'utilisateurcliquesurl'icônefavori
 */
class PlaceAdapter(
private val onItemClick: (Place)->Unit,
private val onFavoriteClick:(Place)->Unit
):ListAdapter<Place,PlaceAdapter.PlaceViewHolder>(PlaceDiffCallback()){
// ──ViewHolder────────────────────────────────────────────────────
    inner class PlaceViewHolder(private val binding:ItemPlaceBinding)
    :RecyclerView.ViewHolder(binding.root){
        fun bind(place:Place){
//Textes
            binding.tvPlaceName.text =place.name
            binding.tvPlaceAddress.text=place.address.ifBlank{"Adressenondisponible"}
            binding.tvDistance.text =place.formattedDistance()?:""
//Chipcatégorie
            binding.chipCategory.text="${place.category.icon}${place.category.label}"
//PhotoavecCoil(chargementasynchrone+placeholder)
            if(!place.photoUrl.isNullOrBlank()){
                binding.ivPlacePhoto.load(place.photoUrl){
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                    transformations(RoundedCornersTransformation(8f))
                }
            }else{
//Placeholdersipasdephoto
                binding.ivPlacePhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }
//Clicsurlacarte →détaildulieu
            binding.cardPlace.setOnClickListener{onItemClick(place)}
//Clicsurleboutonfavori
            binding.btnFavorite.setOnClickListener{onFavoriteClick(place)}
        }
    }
// ──Méthodesdel'adapter─────────────────────────────────────────
    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):PlaceViewHolder{
        val binding=ItemPlaceBinding.inflate(
            LayoutInflater.from(parent.context),parent,false
        )
        return PlaceViewHolder(binding)
    }
    override fun onBindViewHolder(holder:PlaceViewHolder,position:Int){
        holder.bind(getItem(position))
    }
// ──Mettreàjourl'icônefavorid'unitemprécis────────────────
    fun updateFavoriteIcon(placeId:String, isFavorite:Boolean){
        val position=currentList.indexOfFirst{it.id==placeId}
        if(position!=-1)notifyItemChanged(position, isFavorite)
    }
// ──DiffUtil.ItemCallback─────────────────────────────────────────
    class PlaceDiffCallback:DiffUtil.ItemCallback<Place>(){
//ComparerparID(identitédel'objet)
        override fun areItemsTheSame(old:Place,new:Place)=old.id==new.id
//Comparerlecontenucomplet(miseàjourdel'affichage)
        override fun areContentsTheSame(old:Place,new:Place)=old==new
    }
}