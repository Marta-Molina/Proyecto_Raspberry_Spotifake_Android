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

                // Center clear image
                Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.user)
                    .circleCrop()
                    .into(binding.imgArtistProfile)

                // Blurred banner image
                Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .transform(jp.wasabeef.glide.transformations.BlurTransformation(25, 3))
                    .into(binding.imgBanner)
                
                updateFollowButton(it.siguiendo)
            }
        }

        viewModel.popularSongs.observe(viewLifecycleOwner) { songs ->
            popularSongsAdapter.updateList(songs)
        }

        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            // Sort by date (descending) if available
            val sortedAlbums = albums.sortedByDescending { it.fechaLanzamiento }
            albumsAdapter.update(sortedAlbums)
        }
    }

    private fun updateFollowButton(isFollowing: Boolean) {
        if (isFollowing) {
            binding.btnFollow.text = "SIGUIENDO"
            binding.btnFollow.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.btnFollow.setStrokeColorResource(android.R.color.white)
        } else {
            binding.btnFollow.text = "SEGUIR"
            binding.btnFollow.setBackgroundColor(android.graphics.Color.parseColor("#1DB954")) // Spotify Green
            binding.btnFollow.setStrokeColorResource(android.R.color.transparent)
        }
    }

    private fun toggleFollow() {
        val artista = viewModel.currentArtista.value ?: return
        val currentFollowing = artista.siguiendo
        
        if (currentFollowing) {
            viewModel.unfollowArtista(artista.id)
        } else {
            viewModel.followArtista(artista.id)
        }
        
        // Optimistic update
        val updatedArtista = artista.copy(
            siguiendo = !currentFollowing,
            seguidores = if (currentFollowing) artista.seguidores - 1 else artista.seguidores + 1
        )
        // Note: we don't update VM here directly usually, but let's assume VM reload will happen or we update locally
        updateFollowButton(!currentFollowing)
        binding.txtFollowers.text = formatCount(updatedArtista.seguidores)
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
            count >= 1_000 -> String.format("%.1fK", count / 1_000f)
            else -> count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
