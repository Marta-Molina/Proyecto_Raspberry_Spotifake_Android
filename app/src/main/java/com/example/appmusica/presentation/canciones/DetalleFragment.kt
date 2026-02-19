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
                val fullPortadaUrl = if (portadaPath.startsWith("http")) {
                    portadaPath
                } else {
                    "${com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("api/")}${portadaPath.removePrefix("/")}"
                }
                
                val glideUrl = GlideUrl(fullPortadaUrl, LazyHeaders.Builder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build())

                Glide.with(requireContext())
                    .load(glideUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.imgCancion)

                it.urlAudio?.let { audioUrl ->
                    setupPlayer(audioUrl)
                    binding.playerView.visibility = View.VISIBLE
                } ?: run {
                    binding.playerView.visibility = View.GONE
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer(audioUrl: String) {
        if (player == null) {
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(mapOf("ngrok-skip-browser-warning" to "true"))
            
            player = ExoPlayer.Builder(requireContext())
                .setMediaSourceFactory(DefaultMediaSourceFactory(requireContext()).setDataSourceFactory(dataSourceFactory))
                .build()
            
            binding.playerView.player = player
            // Aseguramos que los controles se muestren por defecto
            binding.playerView.showController()
            binding.playerView.controllerAutoShow = true
        }
        
        val fullAudioUrl = if (audioUrl.startsWith("http")) {
            audioUrl
        } else {
            "${com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("api/")}${audioUrl.removePrefix("/")}"
        }
        
        Log.d("PLAYER", "Playing: $fullAudioUrl")
        val mediaItem = MediaItem.fromUri(fullAudioUrl)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
        _binding = null
    }
}
