// ui/favorites/FavoriteAdapter.kt
package com.citypulse.app.ui.favorites
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.citypulse.app.databinding.ItemFavoriteBinding
import com.citypulse.app.domain.model.Favorite
class FavoriteAdapter(
    private val onItemClick: (Favorite) -> Unit,
    private val onDeleteClick:(Favorite) -> Unit
) : ListAdapter<Favorite, FavoriteAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {
    inner class FavoriteViewHolder(private val binding: ItemFavoriteBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(favorite: Favorite) {
            val place = favorite.place
            binding.tvPlaceName.text = place.name
            binding.chipCategory.text = "${place.category.icon} ${place.category.label}"
// Note personnelle (masquee si vide)
            if (favorite.hasNote) {
                binding.tvNote.text = favorite.note
                binding.tvNote.isVisible = true
            } else {
                binding.tvNote.isVisible = false
            }
// Photo
            if (!place.photoUrl.isNullOrBlank()) {
                binding.ivPlacePhoto.load(place.photoUrl) { crossfade(true) }
            }
            binding.cardFavorite.setOnClickListener { onItemClick(favorite) }
            binding.btnDelete.setOnClickListener { onDeleteClick(favorite) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FavoriteViewHolder(binding)
    }
    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class FavoriteDiffCallback : DiffUtil.ItemCallback<Favorite>() {
        override fun areItemsTheSame(old: Favorite, new: Favorite) =
            old.placeId == new.placeId
        override fun areContentsTheSame(old: Favorite, new: Favorite) = old == new
    }
}