package com.dktech.baseandroidviewdktech.base.bottomsheet

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.bottomsheet.adapter.MusicAdapter
import com.dktech.baseandroidviewdktech.base.bottomsheet.model.MusicItem
import com.dktech.baseandroidviewdktech.databinding.SelectMusicBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class SelectMusicBottomSheet(
    private val currentSelectedMusic: MusicItem? = null,
    private val onMusicSelected: ((MusicItem?) -> Unit)? = null,
) : BottomSheetDialogFragment() {
    companion object {
        var lastIsOnlineTab: Boolean = true
    }

    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: SelectMusicBottomsheetBinding? = null
    private val binding get() = _binding!!

    private var isOnlineTab = lastIsOnlineTab
    private var musicAdapter: MusicAdapter? = null
    private var localMusicList = mutableListOf<MusicItem>()
    private var selectedMusic: MusicItem? = currentSelectedMusic
    private var previewPlayer: MediaPlayer? = null
    private var previewPlayingItem: MusicItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.decorView?.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        setWhiteNavigationBar(dialog)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SelectMusicBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadLocalMusic()
        updateMusicList(localMusicList)
        setupCloseButton()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupRecyclerView() {
        musicAdapter =
            MusicAdapter(
                musicList = emptyList(),
                selectedItem = selectedMusic,
                onUseClick = { musicItem ->
                    selectedMusic = musicItem
                    onMusicSelected?.invoke(musicItem)
                    dismiss()
                },
                onRemoveClick = { musicItem ->
                    if (selectedMusic == musicItem) {
                        selectedMusic = null
                        onMusicSelected?.invoke(null)
                        musicAdapter?.setSelectedItem(null)
                    }
                },
                isOnlineTab = isOnlineTab,
                onAvatarClick = { musicItem ->
                    handlePreviewClick(musicItem)
                },
                isItemPlaying = { musicItem ->
                    previewPlayingItem == musicItem && previewPlayer?.isPlaying == true
                },
            )
        binding.rcvMusic.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvMusic.adapter = musicAdapter
    }

    private fun loadLocalMusic() {
        localMusicList.clear()

        val rawMusicFiles =
            listOf<Triple<Int, String, Int>>(
                Triple(R.raw.m1, "Days of Serenity", R.drawable.music_0),
                Triple(R.raw.m2, "Dream Ship", R.drawable.music_1),
                Triple(R.raw.m3, "Dreamy", R.drawable.music_2),
                Triple(R.raw.m4, "Forest Memories", R.drawable.music_3),
                Triple(R.raw.m5, "Morning Coffee", R.drawable.music_4),
                Triple(R.raw.m6, "Meadow", R.drawable.music_5),
                Triple(R.raw.m7, "Quiet", R.drawable.music_6),
                Triple(R.raw.m8, "Rainy", R.drawable.music_7),
                Triple(R.raw.m9, "Study", R.drawable.music_8),
                Triple(R.raw.m10, "Roadtrip", R.drawable.music_9),
            )

        rawMusicFiles.forEach { (resourceId, name, thumbnail) ->
            localMusicList.add(
                MusicItem(
                    name = name,
                    artistName = "None",
                    resourceId = resourceId,
                    thumbnail = thumbnail,
                ),
            )
        }
    }

    private fun updateMusicList(list: List<MusicItem>) {
        musicAdapter =
            MusicAdapter(
                musicList = list,
                selectedItem = selectedMusic,
                onUseClick = { musicItem ->
                    selectedMusic = musicItem
                    onMusicSelected?.invoke(musicItem)
                    dismiss()
                },
                onRemoveClick = { musicItem ->
                    if (selectedMusic == musicItem) {
                        selectedMusic = null
                        onMusicSelected?.invoke(null)
                        musicAdapter?.setSelectedItem(null)
                    }
                },
                isOnlineTab = isOnlineTab,
                onAvatarClick = { musicItem ->
                    handlePreviewClick(musicItem)
                },
                isItemPlaying = { musicItem ->
                    previewPlayingItem == musicItem && previewPlayer?.isPlaying == true
                },
            )
        binding.rcvMusic.adapter = musicAdapter
    }

    private fun handlePreviewClick(musicItem: MusicItem) {
        // Nếu đang play/chọn chính bài đó -> toggle play/pause
        if (previewPlayingItem == musicItem && previewPlayer != null) {
            previewPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.start()
                }
            }
            musicAdapter?.notifyDataSetChanged()
            return
        }

        // Đổi sang bài khác hoặc đang stop -> play mới
        previewPlayer?.release()
        previewPlayer = null

        try {
            previewPlayer =
                if (musicItem.resourceId != null) {
                    // Nhạc từ raw (tab Online hiện tại dùng localMusicList)
                    MediaPlayer.create(requireContext(), musicItem.resourceId!!).apply {
                        setOnCompletionListener {
                            previewPlayingItem = null
                            musicAdapter?.notifyDataSetChanged()
                        }
                        start()
                    }
                } else if (!musicItem.filePath.isNullOrEmpty()) {
                    val file = File(musicItem.filePath!!)
                    if (file.exists()) {
                        MediaPlayer().apply {
                            setDataSource(requireContext(), Uri.fromFile(file))
                            setOnPreparedListener { mp ->
                                mp.start()
                                musicAdapter?.notifyDataSetChanged()
                            }
                            setOnCompletionListener {
                                previewPlayingItem = null
                                musicAdapter?.notifyDataSetChanged()
                                release()
                                previewPlayer = null
                            }
                            prepareAsync()
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }

            previewPlayingItem = musicItem
        } catch (_: Exception) {
            previewPlayer?.release()
            previewPlayer = null
            previewPlayingItem = null
        }

        // Cập nhật lại icon play/pause trong list
        musicAdapter?.notifyDataSetChanged()
    }

    private fun setupCloseButton() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        previewPlayer?.release()
        previewPlayer = null
        previewPlayingItem = null
        _binding = null
    }

    private fun setWhiteNavigationBar(dialog: Dialog) {
        val window: Window? = dialog.window
        if (window != null) {
            val metrics = DisplayMetrics()
            window.windowManager.defaultDisplay.getMetrics(metrics)
            val dimDrawable = GradientDrawable()
            val navigationBarDrawable = GradientDrawable()
            navigationBarDrawable.shape = GradientDrawable.RECTANGLE
            navigationBarDrawable.setColor("#000000".toColorInt())
            val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)
            val windowBackground = LayerDrawable(layers)
            windowBackground.setLayerInsetTop(
                1,
                metrics.heightPixels - navigationBarDrawable.intrinsicHeight - 50,
            )
            window.setBackgroundDrawable(windowBackground)
        }
    }
}
