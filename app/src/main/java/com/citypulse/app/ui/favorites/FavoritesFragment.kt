// ui/favorites/FavoritesFragment.kt
package com.citypulse.app.ui.favorites
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.citypulse.app.databinding.FragmentFavoritesBinding
import com.citypulse.app.repository.RepositoryProvider
import com.citypulse.app.viewmodel.FavoritesViewModel
import com.citypulse.app.viewmodel.FavoritesViewModelFactory
import com.citypulse.app.viewmodel.FavoritesEvent
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(RepositoryProvider.favoriteRepository(requireContext()))
    }
    private lateinit var favoriteAdapter: FavoriteAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToDelete()
        observeViewModel()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // ── RecyclerView ──────────────────────────────────────────────────
    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter(
            onItemClick = { fav -> viewModel.onFavoriteClicked(fav) },
            onDeleteClick = { fav -> viewModel.deleteFavorite(fav) }
        )
        binding.recyclerFavorites.adapter = favoriteAdapter
    }
    // ── Swipe-to-delete avec ItemTouchHelper ──────────────────────────
    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, // dragDirs = 0 : pas de drag-and-drop
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // swipe gauche ET droite
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val favorite = favoriteAdapter.currentList[position]
                viewModel.deleteFavorite(favorite)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerFavorites)
    }
    // ── Observateurs ─────────────────────────────────────────────────
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
// Liste des favoris
                launch {
                    viewModel.favorites.collect { favorites ->
                        favoriteAdapter.submitList(favorites)
                        binding.layoutEmptyFavorites.isVisible = favorites.isEmpty()
                        binding.recyclerFavorites.isVisible = favorites.isNotEmpty()
                    }
                }
// Evenements ponctuels
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is FavoritesEvent.ShowUndo -> {
// Snackbar avec bouton Annuler
                                Snackbar.make(
                                    binding.root,
                                    "${event.placeName} retire des favoris",
                                    Snackbar.LENGTH_LONG
                                ).setAction("Annuler") {
                                    viewModel.undoDelete()
                                }.show()
                            }
                            is FavoritesEvent.NavigateToDetail -> {
// Navigation Jour 7 -- implementee ici
                                android.util.Log.d("FAV", "Naviguer vers ${event.placeId}")
                            }
                        }
                    }
                }
            }
        }
    }
}