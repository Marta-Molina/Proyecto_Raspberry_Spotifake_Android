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

        viewModel.selectCancion(position)

        viewModel.selectedCancion.observe(viewLifecycleOwner) { cancion ->
            cancion?.let {
                binding.txtNombre.text = it.nombre
                binding.txtArtista.text = it.artista
                binding.txtAlbum.text = it.album

                val portadaPath = it.urlPortada ?: ""
                val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("/")
                val fullPortadaUrl = if (portadaPath.startsWith("http")) portadaPath else baseUrl + portadaPath
                
                val glideUrl = GlideUrl(fullPortadaUrl, LazyHeaders.Builder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build())

                Glide.with(this)
                    .load(glideUrl)
                    .centerCrop()
                    .placeholder(R.drawable.portada_generica)
                    .error(R.drawable.portada_generica)
                    .into(binding.imgCancion)

                // The controller might not be ready yet, so we'll try to setup when it is
                trySetupPlayerWithCurrentCancion()
            }
        }

        setupManualControls()
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
    }

    private fun trySetupPlayerWithCurrentCancion() {
        val controller = mediaController ?: return
        val cancion = viewModel.selectedCancion.value ?: return
        
        cancion.urlAudio?.let { audioUrl ->
            val portadaPath = cancion.urlPortada ?: ""
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("/")
            val fullPortadaUrl = if (portadaPath.startsWith("http")) portadaPath else baseUrl + portadaPath

            val metadata = MediaMetadata.Builder()
                .setTitle(cancion.nombre)
                .setArtist(cancion.artista)
                .setAlbumTitle(cancion.album)
                .setArtworkUri(android.net.Uri.parse(fullPortadaUrl))
                .build()

            val fullAudioUrl = if (audioUrl.startsWith("http")) audioUrl else baseUrl + audioUrl

            if (controller.currentMediaItem?.localConfiguration?.uri?.toString() == fullAudioUrl) {
                return
            }

            val mediaItem = androidx.media3.common.MediaItem.Builder()
                .setUri(fullAudioUrl)
                .setMediaMetadata(metadata)
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.playWhenReady = true
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlaybackService::class.java))
        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            // controllerFuture.get() is safe here because the listener is only called when ready
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
            })
            updatePlayPauseIcon()
            if (controller.isPlaying) {
                binding.root.post(updateProgressRunnable)
            }
            
            // Now that controller is ready, try to setup the player if we have a song selected
            trySetupPlayerWithCurrentCancion()
        }, requireContext().mainExecutor)
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
