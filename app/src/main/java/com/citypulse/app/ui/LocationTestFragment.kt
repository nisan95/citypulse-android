// ui/LocationTestFragment.kt — TEMPORAIRE, supprimé Jour 3
package com.citypulse.app.ui
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.citypulse.app.data.local.FusedLocationRepository
import com.citypulse.app.viewmodel.LocationViewModel
import com.citypulse.app.viewmodel.LocationViewModelFactory
import kotlinx.coroutines.launch
class LocationTestFragment : Fragment() {
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(FusedLocationRepository(requireContext().applicationContext))
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        TextView(requireContext()).apply { text = "Récupération position..."; setPadding(48,48,48,48) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tv = view as TextView
        viewModel.fetchLastKnownLocation()
        viewLifecycleOwner.lifecycleScope.launch {
// repeatOnLifecycle : suspend en background, reprend en foreground
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentLocation.collect { loc ->
                        tv.text = viewModel.formatLocation(loc) ?: "GPS en cours..."
                    }
                }
                launch {
                    viewModel.errorMessage.collect { err -> tv.text = "Erreur : $err" }
                }
            }
        }
    }
    override fun onResume() { super.onResume(); viewModel.startLocationUpdates() }
    override fun onPause() { super.onPause(); viewModel.stopLocationUpdates() }
}