package com.example.appmusica.presentation.canciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentDetalleBinding
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetalleFragment : Fragment() {

    private var _binding: FragmentDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by viewModels()
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
                val fullPortadaUrl = "${com.example.appmusica.di.NetworkModule.BASE_URL}${portadaPath.removePrefix("/")}"
                Glide.with(requireContext())
                    .load(fullPortadaUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.imgCancion)

                it.urlAudio?.let { audioUrl ->
                    setupPlayer(audioUrl)
                } ?: run {
                    // Handle case where there is no audio URL
                    binding.playerControlView.visibility = View.GONE
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer(audioUrl: String) {
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.playerControlView.player = exoPlayer
            val fullAudioUrl = "${com.example.appmusica.di.NetworkModule.BASE_URL}${audioUrl.removePrefix("/")}"
            val mediaItem = MediaItem.fromUri(fullAudioUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
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
