package com.example.appmusica.presentation.canciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentAlbumSongsBinding
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.bumptech.glide.Glide
import com.example.appmusica.di.NetworkModule
import androidx.navigation.fragment.findNavController
import com.example.appmusica.util.setClickAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumSongsFragment : Fragment() {

    private var _binding: FragmentAlbumSongsBinding? = null
    private val binding get() = _binding!!

    @javax.inject.Inject
    lateinit var likedSongsManager: com.example.appmusica.data.local.LikedSongsManager

    private val viewModel: CancionesViewModel by activityViewModels()
    private lateinit var adapter: AdapterCancion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumId = arguments?.getInt("albumId") ?: -1

        adapter = AdapterCancion(
            list = mutableListOf(),
            delete = { pos -> viewModel.deleteCancion(adapter.getCancion(pos)?.id ?: -1) },
            update = { pos -> /* no-op */ },
            like = { pos -> 
                val cancion = adapter.getCancion(pos)
                if (cancion != null) {
                    val isCurrentlyLiked = likedSongsManager.isLiked(cancion.id)
                    likedSongsManager.toggleLike(cancion.id)
                    viewModel.toggleLike(cancion, isCurrentlyLiked)
                    adapter.notifyItemChanged(pos)
                }
            },
            addToList = { pos -> /* can add items from album to playlists if needed */ },
            onItemClick = { pos -> navegarADetalle(pos) },
            isLiked = { cancionId -> likedSongsManager.isLiked(cancionId) }
        )

        binding.recyclerAlbumSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlbumSongsFragment.adapter
        }

        // Observar canciones del álbum y cargarlas
        viewModel.albumSongs.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)
            viewModel.setCanciones(lista)
        }

        viewModel.currentAlbum.observe(viewLifecycleOwner) { album ->
            album?.let {
                binding.txtAlbumTitleLarge.text = it.nombre
                binding.txtAlbumArtistLarge.text = it.artistasNombre?.joinToString(", ") ?: ""

                val baseUrl = NetworkModule.BASE_API_URL.removeSuffix("/")
                val fullUrl = if (it.portadaUrl?.startsWith("http") == true) it.portadaUrl else baseUrl + it.portadaUrl

                Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.portada_generica)
                    .into(binding.imgAlbumCoverLarge)
            }
        }

        viewModel.loadCancionesForAlbum(albumId)

        binding.btnPlayAlbum.setOnClickListener {
            if (adapter.itemCount > 0) {
                navegarADetalle(0)
            }
        }
        binding.btnPlayAlbum.setClickAnimation()
    }

    private fun navegarADetalle(position: Int) {
        (activity as? com.example.appmusica.presentation.MainActivity)?.expandPlayer(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
