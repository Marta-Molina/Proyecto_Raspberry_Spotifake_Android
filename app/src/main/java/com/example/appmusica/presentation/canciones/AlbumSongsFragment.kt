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
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumSongsFragment : Fragment() {

    private var _binding: FragmentAlbumSongsBinding? = null
    private val binding get() = _binding!!

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
            like = { pos -> viewModel.toggleLike(adapter.getCancion(pos)!!) },
            addToList = { pos -> /* can add items from album to playlists if needed */ },
            onItemClick = { pos -> navegarADetalle(pos) },
            isLiked = { _ -> false }
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

        viewModel.loadCancionesForAlbum(albumId)
    }

    private fun navegarADetalle(position: Int) {
        (activity as? com.example.appmusica.presentation.MainActivity)?.expandPlayer(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
