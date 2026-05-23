package com.example.appmusica.presentation.artista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.bumptech.glide.Glide
import com.example.appmusica.databinding.FragmentArtistaDetalleBinding
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.adapter.AlbumAdapter
import com.example.appmusica.di.NetworkModule
import com.example.appmusica.util.FormatUtils
import com.example.appmusica.util.setClickAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistaDetalleFragment : Fragment() {

    private var _binding: FragmentArtistaDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by activityViewModels()
    private var artistId: Int = -1

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

        artistId = arguments?.getInt("artistId") ?: -1

        setupRecyclerViews()
        observeViewModel()

        viewModel.loadArtistaDetalle(artistId)

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
            isLiked = { id -> true }, // Simplified or use likedSongsManager
            addToQueue = { pos ->
                popularSongsAdapter.getCancion(pos)?.let { viewModel.addToQueue(it) }
            },
            playNext = { pos ->
                popularSongsAdapter.getCancion(pos)?.let { viewModel.playNext(it) }
            }
        )
        binding.recyclerPopularSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPopularSongs.adapter = popularSongsAdapter

        // Albums
        albumsAdapter = AlbumAdapter(mutableListOf()) { albumId ->
            val bundle = Bundle().apply { putInt("albumId", albumId) }
            findNavController().navigate(R.id.albumSongsFragment, bundle)
        }
        binding.recyclerAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAlbums.adapter = albumsAdapter
    }

    private fun observeViewModel() {
        viewModel.currentArtista.observe(viewLifecycleOwner) { artista ->
            artista?.let {
                binding.txtArtistNameCaps.text = it.nombre.uppercase()
                binding.txtFollowers.text = FormatUtils.formatCount(it.seguidores)
                binding.txtTotalLikes.text = FormatUtils.formatCount(it.likesTotales)

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

                if (it.siguiendo) {
                    showFollowConfetti()
                }
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
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

        if (isFollowing) {
            binding.btnFollow.text = "SIGUIENDO"
            binding.btnFollow.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.btnFollow.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE))
            binding.btnFollow.setTextColor(android.graphics.Color.WHITE)
        } else {
            binding.btnFollow.text = "SEGUIR"
            binding.btnFollow.setBackgroundColor(primaryColor)
            binding.btnFollow.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT))
            binding.btnFollow.setTextColor(android.graphics.Color.BLACK) // Or white depending on theme? Default is green with black text usually
        }
    }

    private var userTriggeredFollow = false

    private fun toggleFollow() {
        val artista = viewModel.currentArtista.value ?: return
        val currentFollowing = artista.siguiendo

        if (currentFollowing) {
            userTriggeredFollow = false
            viewModel.unfollowArtista(artista.id)
        } else {
            userTriggeredFollow = true
            viewModel.followArtista(artista.id)
        }
        // ViewModel handles optimistic update through LiveData now
    }

    private fun showFollowConfetti() {
        if (userTriggeredFollow) {
            binding.konfettiView.start(com.example.appmusica.util.ConfettiManager(requireContext()).getPartyForCurrentTheme())
            userTriggeredFollow = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
