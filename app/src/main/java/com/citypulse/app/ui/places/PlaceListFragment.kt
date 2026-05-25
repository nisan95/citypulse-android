package com.citypulse.app.ui.places

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.citypulse.app.data.local.FusedLocationRepository
import com.citypulse.app.databinding.FragmentPlaceListBinding
import com.citypulse.app.domain.model.Category
import com.citypulse.app.domain.model.Place
import com.citypulse.app.repository.RepositoryProvider
import com.citypulse.app.viewmodel.LocationViewModel
import com.citypulse.app.viewmodel.LocationViewModelFactory
import com.citypulse.app.viewmodel.PlaceListViewModel
import com.citypulse.app.viewmodel.PlaceListViewModelFactory
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class PlaceListFragment : Fragment() {

    private var _binding: FragmentPlaceListBinding? = null
    private val binding get() = _binding!!

    private val locationViewModel: LocationViewModel by activityViewModels {
        LocationViewModelFactory(FusedLocationRepository(requireContext().applicationContext))
    }

    private val viewModel: PlaceListViewModel by viewModels {
        PlaceListViewModelFactory(
            placeRepository = RepositoryProvider.placeRepository(requireContext()),
            favoriteRepository = RepositoryProvider.favoriteRepository(requireContext()),
            locationViewModel = locationViewModel
        )
    }

    private lateinit var placeAdapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ): View {
        _binding = FragmentPlaceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaces()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceAdapter(
            onItemClick = { place -> navigateToDetail(place) },
            onFavoriteClick = { place -> viewModel.toggleFavorite(place) }
        )
        binding.recyclerPlaces.adapter = placeAdapter
    }

    private fun setupCategoryChips() {
        val allChip = createChip("Tous", null)
        binding.chipGroupCategories.addView(allChip)
        Category.entries.forEach { category ->
            val chip = createChip("${category.icon} ${category.label}", category)
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun createChip(label: String, category: Category?): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.filterByCategory(category)
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onSearchQueryChanged(newText ?: "")
                    return true
                }
            }
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state -> renderState(state) }
                }
                launch {
                    viewModel.events.collect { event -> handleEvent(event) }
                }
            }
        }
    }

    private fun renderState(state: PlaceListUiState) {
        binding.progressBar.isVisible = state is PlaceListUiState.Loading
        binding.recyclerPlaces.isVisible = state is PlaceListUiState.Success
        binding.layoutEmpty.isVisible = state is PlaceListUiState.Empty
        binding.layoutError.isVisible = state is PlaceListUiState.Error

        when (state) {
            is PlaceListUiState.Success -> {
                placeAdapter.submitList(state.places)
            }
            is PlaceListUiState.Empty -> {
                binding.tvEmptySubtitle.text = state.reason
            }
            is PlaceListUiState.Error -> {
                binding.tvErrorMessage.text = state.message
                binding.btnRetry.setOnClickListener { viewModel.loadPlaces() }
            }
            is PlaceListUiState.Loading -> { /* ProgressBar visible */ }
        }
    }

    private fun handleEvent(event: PlaceListEvent) {
        when (event) {
            is PlaceListEvent.NavigateToDetail -> navigateToDetail(event.place)
            is PlaceListEvent.ShowMessage -> {
                com.google.android.material.snackbar.Snackbar
                    .make(binding.root, event.message,
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun navigateToDetail(place: Place) {
        android.util.Log.d("LIST", "Naviguer vers détail : ${place.name}")
    }
}