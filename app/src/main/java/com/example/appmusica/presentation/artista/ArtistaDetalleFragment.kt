package com.example.appmusica.presentation.artista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentArtistaDetalleBinding
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.adapter.AlbumAdapter
import com.example.appmusica.di.NetworkModule
import com.example.appmusica.util.setClickAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistaDetalleFragment : Fragment() {

    private var _binding: FragmentArtistaDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by activityViewModels()
    private val args: ArtistaDetalleFragmentArgs by navArgs()

    private lateinit var popularSongsAdapter: AdapterCancion
    private lateinit var albumsAdapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistaDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()

        viewModel.loadArtistaDetalle(args.artistId)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnFollow.setOnClickListener {
            toggleFollow()
        }
        binding.btnFollow.setClickAnimation()
    }

    private fun setupRecyclerViews() {
        // Popular Songs
        popularSongsAdapter = AdapterCancion(
            list = mutableListOf(),
            delete = { /* No delete in artist view */ },
            update = { /* No update in artist view */ },
            like = { pos -> 
                val cancion = popularSongsAdapter.getCancion(pos)
                cancion?.let { viewModel.addLike(it) }
            },
            addToList = { pos ->
                // TODO: Implement add to list
            },
            onItemClick = { pos ->
                // Expandir player con la canción seleccionada
                (activity as? com.example.appmusica.presentation.MainActivity)?.expandPlayer(pos)
            },
            isLiked = { id -> true } // Simplified or use likedSongsManager
        )
        binding.recyclerPopularSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPopularSongs.adapter = popularSongsAdapter

        // Albums
        albumsAdapter = AlbumAdapter(mutableListOf()) { albumId ->
            val action = ArtistaDetalleFragmentDirections.actionArtistaDetalleFragmentToAlbumSongsFragment(albumId)
            findNavController().navigate(action)
        }
        binding.recyclerAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAlbums.adapter = albumsAdapter
    }

    private fun observeViewModel() {
        viewModel.currentArtista.observe(viewLifecycleOwner) { artista ->
            artista?.let {
                binding.txtArtistNameCaps.text = it.nombre.uppercase()
                binding.txtFollowers.text = "${it.seguidores} Seguidores"
                binding.txtTotalLikes.text = "${it.likesTotales} Likes"

                val baseUrl = NetworkModule.BASE_API_URL.removeSuffix("/")
                val fullUrl = if (it.fotoUrl?.startsWith("http") == true) it.fotoUrl else baseUrl + it.fotoUrl

                Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.user)
                    .circleCrop()
                    .into(binding.imgArtistProfile)

                Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .into(binding.imgBanner)
            }
        }

        viewModel.popularSongs.observe(viewLifecycleOwner) { songs ->
            popularSongsAdapter.updateList(songs)
        }

        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            albumsAdapter.update(albums)
        }
    }

    private fun toggleFollow() {
        val artista = viewModel.currentArtista.value ?: return
        
        // Check current state (Simple text check for now, ideally Artista model has isFollowing)
        if (binding.btnFollow.text == "SEGUIR") {
            viewModel.followArtista(artista.id)
            binding.btnFollow.text = "SIGUIENDO"
            binding.btnFollow.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.btnFollow.setStrokeColorResource(android.R.color.white)
        } else {
            viewModel.unfollowArtista(artista.id)
            binding.btnFollow.text = "SEGUIR"
            // Restore original style or just toggle text
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
