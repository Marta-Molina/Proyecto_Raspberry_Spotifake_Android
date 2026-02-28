package com.example.appmusica.presentation.canciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.example.appmusica.service.PlaybackService
import android.content.ComponentName
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentDetalleBinding
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.util.setClickAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetalleFragment : Fragment() {

    private var _binding: FragmentDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by activityViewModels()
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val player: Player? get() = mediaController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var updateProgressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            binding.root.postDelayed(this, 1000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt("position") ?: return

        // Note: we don't call viewModel.selectCancion(position) here if we want to support 
        // external list management, but the current logic uses it for the initial meta.
        viewModel.selectCancion(position)

        viewModel.selectedCancion.observe(viewLifecycleOwner) { cancion ->
            cancion?.let {
                updateUI(it)
                // Try setup, but only if the controller is ready
                trySetupPlayerWithCurrentList()
            }
        }

        setupManualControls()
    }

    private fun updateUI(cancion: com.example.appmusica.domain.model.Cancion) {
        binding.txtNombre.text = cancion.nombre
        binding.txtArtista.text = cancion.artista
        binding.txtAlbum.text = cancion.album

        val portadaPath = cancion.urlPortada ?: ""
        val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("/")
        val fullPortadaUrl = if (portadaPath.startsWith("http")) portadaPath else baseUrl + portadaPath

        val glideUrl = GlideUrl(
            fullPortadaUrl, LazyHeaders.Builder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
        )

        Glide.with(this)
            .load(glideUrl)
            .centerCrop()
            .placeholder(R.drawable.portada_generica)
            .error(R.drawable.portada_generica)
            .into(binding.imgCancion)
    }

    private fun setupManualControls() {
        binding.btnPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
                updatePlayPauseIcon()
            }
        }
        binding.btnPlayPause.setClickAnimation()

        binding.sliderProgress.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                player?.let {
                    val duration = it.duration
                    if (duration > 0) {
                        it.seekTo((value * duration / 100).toLong())
                    }
                }
            }
        }

        binding.btnPrev.setOnClickListener { player?.seekToPrevious() }
        binding.btnPrev.setClickAnimation()
        binding.btnNext.setOnClickListener { player?.seekToNext() }
        binding.btnNext.setClickAnimation()

        binding.btnRepeat.setOnClickListener {
            player?.let {
                it.repeatMode = if (it.repeatMode == Player.REPEAT_MODE_OFF) {
                    Player.REPEAT_MODE_ONE
                } else {
                    Player.REPEAT_MODE_OFF
                }
                updateRepeatIcon()
            }
        }
        binding.btnRepeat.setClickAnimation()
    }

    private fun trySetupPlayerWithCurrentList() {
        val controller = mediaController ?: return
        val cancionList = viewModel.canciones.value ?: return
        val initialPosition = arguments?.getInt("position") ?: return

        if (cancionList.isEmpty() || initialPosition >= cancionList.size) return

        val mediaItems = cancionList.map { song ->
            val audioUrl = song.urlAudio ?: ""
            val portadaPath = song.urlPortada ?: ""
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("/")
            val fullAudioUrl = if (audioUrl.startsWith("http")) audioUrl else baseUrl + audioUrl
            val fullPortadaUrl = if (portadaPath.startsWith("http")) portadaPath else baseUrl + portadaPath

            val metadata = MediaMetadata.Builder()
                .setTitle(song.nombre)
                .setArtist(song.artista)
                .setAlbumTitle(song.album)
                .setArtworkUri(android.net.Uri.parse(fullPortadaUrl))
                .build()

            androidx.media3.common.MediaItem.Builder()
                .setUri(fullAudioUrl)
                .setMediaMetadata(metadata)
                .build()
        }

        // Check if we already have this list loaded to avoid restarting
        val currentPlayingUri = controller.currentMediaItem?.localConfiguration?.uri?.toString()
        val targetUri = mediaItems[initialPosition].localConfiguration?.uri?.toString()

        if (currentPlayingUri == targetUri) {
            return
        }

        controller.setMediaItems(mediaItems, initialPosition, 0)
        controller.prepare()
        controller.playWhenReady = true
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlaybackService::class.java))
        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            val controller = controllerFuture?.get() ?: return@addListener
            mediaController = controller
            binding.playerView.player = controller
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseIcon()
                    if (isPlaying) {
                        binding.root.post(updateProgressRunnable)
                    } else {
                        binding.root.removeCallbacks(updateProgressRunnable)
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    updatePlayPauseIcon()
                }

                override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    // Update UI when song changes (auto-next or manual next/prev)
                    mediaItem?.mediaMetadata?.let { metadata ->
                        binding.txtNombre.text = metadata.title
                        binding.txtArtista.text = metadata.artist
                        binding.txtAlbum.text = metadata.albumTitle
                        
                        metadata.artworkUri?.let { uri ->
                            val glideUrl = GlideUrl(uri.toString(), LazyHeaders.Builder()
                                .addHeader("ngrok-skip-browser-warning", "true")
                                .build())
                            Glide.with(this@DetalleFragment)
                                .load(glideUrl)
                                .centerCrop()
                                .placeholder(R.drawable.portada_generica)
                                .into(binding.imgCancion)
                        }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    updateRepeatIcon()
                }
            })
            updatePlayPauseIcon()
            updateRepeatIcon()
            if (controller.isPlaying) {
                binding.root.post(updateProgressRunnable)
            }
            
            trySetupPlayerWithCurrentList()
        }, requireContext().mainExecutor)
    }

    private fun updateRepeatIcon() {
        val repeatMode = player?.repeatMode ?: Player.REPEAT_MODE_OFF
        val iconRes = if (repeatMode == Player.REPEAT_MODE_ONE) {
            R.drawable.ic_repeat_one
        } else {
            R.drawable.ic_repeat
        }
        binding.btnRepeat.setImageResource(iconRes)
        
        // Change tint to indicate active state
        val color = if (repeatMode == Player.REPEAT_MODE_ONE) {
            resources.getColor(R.color.spotify_green, null)
        } else {
            resources.getColor(R.color.white, null)
        }
        binding.btnRepeat.setColorFilter(color)
    }

    override fun onStop() {
        super.onStop()
        binding.root.removeCallbacks(updateProgressRunnable)
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        controllerFuture = null
        mediaController = null
    }

    private fun updatePlayPauseIcon() {
        val isPlaying = player?.isPlaying ?: false
        val iconRes = if (isPlaying) {
            androidx.media3.ui.R.drawable.exo_ic_pause_circle_filled
        } else {
            androidx.media3.ui.R.drawable.exo_ic_play_circle_filled
        }
        binding.btnPlayPause.setImageResource(iconRes)
    }

    private fun updateProgress() {
        player?.let {
            val current = it.currentPosition
            val duration = it.duration
            if (duration > 0) {
                binding.sliderProgress.value = (current.toFloat() / duration.toFloat() * 100).coerceIn(0f, 100f)
                binding.txtCurrentTime.text = formatTime(current)
                binding.txtTotalTime.text = formatTime(duration)
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
