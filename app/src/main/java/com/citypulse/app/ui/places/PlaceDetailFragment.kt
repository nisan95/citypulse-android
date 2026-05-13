//ui/places/PlaceDetailFragment.kt
package com.citypulse.app.ui.places
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.citypulse.app.databinding.FragmentPlaceDetailBinding
import com.citypulse.app.domain.model.Place
import com.citypulse.app.repository.RepositoryProvider
import com.citypulse.app.viewmodel.LocationViewModel
import com.citypulse.app.viewmodel.LocationViewModelFactory
import com.citypulse.app.viewmodel.PlaceDetailViewModel
import com.citypulse.app.viewmodel.PlaceDetailViewModelFactory
import com.citypulse.app.data.local.FusedLocationRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.citypulse.app.util.SharingHelper
class PlaceDetailFragment:Fragment(){
    private var _binding:FragmentPlaceDetailBinding?=null
    private val binding get() = _binding!!
//IDdulieupasseenargumentparleFragmentappelant
    private val placeId:String by lazy{
        arguments?.getString(ARG_PLACE_ID)
            ?:error("PlaceDetailFragmentrequiertunplaceIddanslesarguments")
    }
    private val locationViewModel:LocationViewModel by activityViewModels{
        LocationViewModelFactory(FusedLocationRepository(requireContext().applicationContext))
    }
    private val viewModel:PlaceDetailViewModel by viewModels{
        PlaceDetailViewModelFactory(
            placeId =placeId,
            placeRepository =RepositoryProvider.placeRepository(requireContext()),
            favoriteRepository=RepositoryProvider.favoriteRepository(requireContext()),
            locationViewModel =locationViewModel
        )
    }
// ──Cycledevie─────────────────────────────────────────────────
    override fun onCreateView(inflater:LayoutInflater,container:ViewGroup?,state:Bundle?):View{
        _binding=FragmentPlaceDetailBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view:View,savedInstanceState:Bundle?){
        super.onViewCreated(view,savedInstanceState)
        setupToolbar()
        setupNotesSaving()
        setupListeners()
        observeViewModel()
    }
    override fun onDestroyView(){
        super.onDestroyView()
        _binding=null
    }
// ──Configurationinitiale────────────────────────────────────────
    private fun setupToolbar(){
        binding.toolbar.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
//Sauvegardeautomatiquedesnotesapres1seconded'inactivite
    private fun setupNotesSaving(){
        binding.etNotes.addTextChangedListener{editable->
            viewModel.onNoteChanged(editable?.toString()?:"")
        }
    }
    private fun setupListeners(){
//FABfavori--toggle
        binding.fabFavorite.setOnClickListener{
            viewModel.toggleFavorite()
        }
//BoutonOuvrirdansMaps
        binding.btnOpenMaps.setOnClickListener{
            viewModel.currentPlace.value?.let{place->
                openInMaps(place)
            }
        }
//BoutonPartager(implémentéJour8parP1)
        binding.btnShare.setOnClickListener{
            viewModel.onShareRequested()
        }
    }
// ──Observateurs─────────────────────────────────────────────────
    private fun observeViewModel(){
        viewLifecycleOwner.lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
//Etatprincipal
                launch{
                    viewModel.uiState.collect{state-> renderState(state)}
                }
//Etatdufavori(iconeFAB)
                launch{
                    viewModel.isFavorite.collect{isFav->updateFabIcon(isFav)}
                }
//Evenementsponctuels
                launch{
                    viewModel.events.collect{event->handleEvent(event)}
                }
            }
        }
    }
// ──Rendudel'etat───────────────────────────────────────────────
    private fun renderState(state:PlaceDetailUiState){
        binding.progressBar.isVisible=state is PlaceDetailUiState.Loading
        if(state is PlaceDetailUiState.Success){
            val place=state.place
            bindPlace(place,state.distanceFormatted,state.savedNote)
        }
        if(state is PlaceDetailUiState.Error){
            Snackbar.make(binding.root,state.message,Snackbar.LENGTH_LONG).show()
        }
    }
    private fun bindPlace(place:Place,distance:String?,savedNote:String){
        binding.tvPlaceName.text =place.name
        binding.tvPlaceAddress.text=place.address.ifBlank{"Adressenondisponible"}
        binding.tvCoordinates.text ="GPS:%.6f,%.6f".format(place.latitude,place.longitude)
        binding.tvDistance.text =distance?:""
        binding.chipCategory.text ="${place.category.icon}${place.category.label}"
//Description(masqueesivide)
        if(!place.description.isNullOrBlank()){
            binding.tvDescription.text =place.description
            binding.tvDescription.isVisible=true
        }
//Photo
        if(!place.photoUrl.isNullOrBlank()){
            binding.ivPlacePhoto.load(place.photoUrl){crossfade(true)}
        }
//Notes(nepasecrasersi l'utilisateurestentraindetaper)
        if(binding.etNotes.text.isNullOrEmpty()&&savedNote.isNotBlank()){
            binding.etNotes.setText(savedNote)
        }
    }
    private fun updateFabIcon(isFavorite:Boolean){
        binding.fabFavorite.setImageResource(
            if(isFavorite)android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }
    private fun handleEvent(event:PlaceDetailEvent){
        when(event){
            is PlaceDetailEvent.FavoriteAdded->
                Snackbar.make(binding.root,"Ajouteauxfavoris",Snackbar.LENGTH_SHORT).show()
            is PlaceDetailEvent.FavoriteRemoved->
                Snackbar.make(binding.root,"Retiredesfavoris",Snackbar.LENGTH_SHORT).show()
            is PlaceDetailEvent.NoteSaved->
                Snackbar.make(binding.root,"Notesauvegardee",Snackbar.LENGTH_SHORT).show()
            is PlaceDetailEvent.ShareRequested->{
                SharingHelper.shareText(requireContext(),event.text)
            }
            is PlaceDetailEvent.ShowError->
                Snackbar.make(binding.root,event.message,Snackbar.LENGTH_LONG).show()
        }
    }
// ──Actions───────────────────────────────────────────────────────
    private fun openInMaps(place:Place){
        val uri =
            Uri.parse("geo:${place.latitude},${place.longitude}?q=${place.latitude},${place.longitude}(${place.name})")
        val intent=Intent(Intent.ACTION_VIEW,uri)
        if(intent.resolveActivity(requireActivity().packageManager)!=null){
            startActivity(intent)
        }else{
//Fallback:ouvrirdanslenavigateur
            startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(place.mapsUrl())))
        }
    }
    private fun sharePlace(text:String){
        val intent=Intent(Intent.ACTION_SEND).apply{
            type="text/plain"
            putExtra(Intent.EXTRA_TEXT,text)
        }
        startActivity(Intent.createChooser(intent,"Partagervia"))
    }
    companion object{
        const val ARG_PLACE_ID="place_id"
        fun newInstance(placeId: String) = PlaceDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_PLACE_ID, placeId) }
        }
    }
}