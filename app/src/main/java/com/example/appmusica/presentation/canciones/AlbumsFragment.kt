package com.example.appmusica.presentation.canciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appmusica.databinding.FragmentAlbumsBinding
import com.example.appmusica.presentation.canciones.adapter.AlbumAdapter
import com.example.appmusica.presentation.canciones.adapter.ArtistAdapter
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import com.example.appmusica.R

@AndroidEntryPoint
class AlbumsFragment : Fragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by activityViewModels()
    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val artistId = arguments?.getInt("artistId") ?: -1

        albumAdapter = AlbumAdapter(emptyList()) { albumId ->
            val bundle = Bundle().apply {
                putInt("albumId", albumId)
            }
            findNavController().navigate(R.id.action_albumsFragment_to_albumSongsFragment, bundle)
        }

        binding.recyclerAlbums.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = albumAdapter
        }

        // Cargar albums desde ViewModel (API)
        viewModel.albums.observe(viewLifecycleOwner) { lista ->
            albumAdapter.update(lista)
        }

        viewModel.loadAlbumsForArtist(artistId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
