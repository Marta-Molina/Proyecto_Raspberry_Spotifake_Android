package com.example.appmusica.presentation.canciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
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
    private var player: ExoPlayer? = null

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

                // BASE_URL already has /api/ — static files are served at /api/archivos/
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

                it.urlAudio?.let { audioUrl ->
                    setupPlayer(audioUrl)
                }
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

    @OptIn(UnstableApi::class)
    private fun setupPlayer(audioUrl: String) {
        if (player == null) {
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(mapOf("ngrok-skip-browser-warning" to "true"))
            
            player = ExoPlayer.Builder(requireContext())
                .setMediaSourceFactory(DefaultMediaSourceFactory(requireContext()).setDataSourceFactory(dataSourceFactory))
                .build()
            
            player?.addListener(object : androidx.media3.common.Player.Listener {
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

            binding.playerView.player = player
        }
        
        // BASE_URL already has /api/ — static files are served at /api/archivos/
        val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("/")
        val fullAudioUrl = if (audioUrl.startsWith("http")) audioUrl else baseUrl + audioUrl

        if (player?.currentMediaItem?.localConfiguration?.uri?.toString() == fullAudioUrl) {
            return
        }
        
        val mediaItem = MediaItem.fromUri(fullAudioUrl)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
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

    override fun onPause() {
        super.onPause()
        binding.root.removeCallbacks(updateProgressRunnable)
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.removeCallbacks(updateProgressRunnable)
        player?.release()
        player = null
        _binding = null
    }
}
