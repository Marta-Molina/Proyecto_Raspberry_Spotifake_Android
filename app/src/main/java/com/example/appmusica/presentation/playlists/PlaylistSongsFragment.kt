package com.example.appmusica.presentation.playlists

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentPlaylistSongsBinding
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.edit.EditCancionActivity
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistSongsFragment : Fragment(R.layout.fragment_playlist_songs) {

    private lateinit var binding: FragmentPlaylistSongsBinding
    private val viewModel: PlaylistViewModel by viewModels()
    private val cancionesViewModel: CancionesViewModel by activityViewModels()
    private lateinit var adapter: AdapterCancion
    private var playlistId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlaylistSongsBinding.bind(view)

        playlistId = arguments?.getInt("playlistId") ?: -1
        val playlistName = arguments?.getString("playlistName") ?: "Lista"

        binding.txtviewPlaylistTitle.text = playlistName

        adapter = AdapterCancion(
            list = mutableListOf(),
            delete = { _ -> /* No global delete in playlist view */ },
            update = { pos ->
                val intent = Intent(requireContext(), EditCancionActivity::class.java)
                intent.putExtra("pos", pos)
                startActivity(intent)
            },
            like = { pos -> 
                // Podríamos inyectar CancionesViewModel aquí también si quisiéramos Likes
            },
            addToList = { _ ->
                // Ya estamos en una lista
            },
            onRemove = { pos ->
                val cancionId = adapter.getCancion(pos)?.id ?: -1
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Quitar canción")
                    .setMessage("¿Estás seguro de que quieres quitar esta canción de la lista?")
                    .setPositiveButton("Quitar") { _, _ ->
                        viewModel.removeSongFromPlaylist(playlistId, cancionId)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            onItemClick = { pos -> navegarADetalle(pos) }
        )

        binding.recyclerPlaylistSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlaylistSongsFragment.adapter
        }

        viewModel.playlistSongs.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)
            cancionesViewModel.setCanciones(lista)
        }

        if (playlistId != -1) {
            viewModel.loadPlaylistSongs(playlistId)
        }
    }

    private fun navegarADetalle(position: Int) {
        (activity as? com.example.appmusica.presentation.MainActivity)?.expandPlayer(position)
    }
}
