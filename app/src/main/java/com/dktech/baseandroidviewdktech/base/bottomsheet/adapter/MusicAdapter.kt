package com.dktech.baseandroidviewdktech.base.bottomsheet.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.bottomsheet.model.MusicItem
import com.dktech.baseandroidviewdktech.databinding.ItemMusicBinding

class MusicAdapter(
    private val musicList: List<MusicItem>,
    private var selectedItem: MusicItem?,
    private val onUseClick: (MusicItem) -> Unit,
    private val onRemoveClick: (MusicItem) -> Unit,
    private val isOnlineTab: Boolean = false,
    private val onAvatarClick: (MusicItem) -> Unit,
    private val isItemPlaying: (MusicItem) -> Boolean,
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {
    fun setSelectedItem(item: MusicItem?) {
        selectedItem = item
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ViewHolder(binding, isOnlineTab)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(musicList[position], position)
    }

    override fun getItemCount(): Int = musicList.size

    inner class ViewHolder(
        private val binding: ItemMusicBinding,
        private val isOnlineTab: Boolean,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            musicItem: MusicItem,
            position: Int,
        ) {
            binding.tvMusic.text = musicItem.name
            binding.tvArtistName.text = musicItem.artistName

            if (isOnlineTab) {
                val avatarIndex = (position % 11) + 1
                val avatarResourceName = "avatar_$avatarIndex"
                val avatarResourceId =
                    binding.root.context.resources.getIdentifier(
                        avatarResourceName,
                        "drawable",
                        binding.root.context.packageName,
                    )
                if (avatarResourceId != 0) {
                    binding.iconAvatar.setImageDrawable(
                        ContextCompat.getDrawable(binding.root.context, avatarResourceId),
                    )
                } else {
                    binding.iconAvatar.setImageResource(R.drawable.ic_music_bottomsheet)
                }
            } else {
                binding.iconAvatar.setImageResource(R.drawable.ic_music_bottomsheet)
            }

            // Hiển thị icon play/pause ở cả tab Online và Local
            binding.iconPlayPause.visibility = View.VISIBLE
            val isPlaying = isItemPlaying(musicItem)
            binding.iconPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause_bottommsheet else R.drawable.ic_play_bottomsheet,
            )

            val isSelected = musicItem == selectedItem
            binding.btnRemove.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            binding.mainContent.setBackgroundColor(
                if (isSelected) {
                    "#DBF2FF".toColorInt()
                } else {
                    Color.WHITE
                },
            )

            binding.btnUse.setOnClickListener {
                onUseClick(musicItem)
            }
            binding.iconAvatar.setOnClickListener {
                onAvatarClick(musicItem)
            }

            binding.btnRemove.setOnClickListener {
                onRemoveClick(musicItem)
            }
        }
    }
}
