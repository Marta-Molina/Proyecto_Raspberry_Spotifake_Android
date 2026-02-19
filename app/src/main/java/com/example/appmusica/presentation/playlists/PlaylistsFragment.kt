package com.example.appmusica.presentation.playlists

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentPlaylistsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private lateinit var binding: FragmentPlaylistsBinding
    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var adapter: PlaylistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlaylistsBinding.bind(view)

        adapter = PlaylistAdapter(
            list = mutableListOf(),
            onDelete = { pos ->
                val playlist = adapter.getPlaylist(pos)
                if (playlist != null) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Eliminar playlist")
                        .setMessage("¿Estás seguro de que quieres eliminar la lista '${playlist.nombre}'?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            viewModel.deletePlaylist(playlist.id)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            },
            onEdit = { pos ->
                adapter.getPlaylist(pos)?.let { mostrarDialogoEditar(it.id, it.nombre) }
            },
            onClick = { pos ->
                adapter.getPlaylist(pos)?.let { navegarACanciones(it.id, it.nombre) }
            }
        )

        binding.recyclerPlaylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlaylistsFragment.adapter
        }

        binding.fabAddPlaylist.setOnClickListener {
            mostrarDialogoCrear()
        }

        viewModel.playlists.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)
        }

        viewModel.loadPlaylists()
    }

    private fun mostrarDialogoCrear() {
        val editText = EditText(requireContext())
        editText.hint = "Nombre de la lista"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Nueva Lista")
            .setView(editText)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = editText.text.toString()
                if (nombre.isNotBlank()) {
                    viewModel.createPlaylist(nombre)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(id: Int, nombreActual: String) {
        val editText = EditText(requireContext())
        editText.setText(nombreActual)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Lista")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString()
                if (nuevoNombre.isNotBlank()) {
                    viewModel.updatePlaylist(id, nuevoNombre)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navegarACanciones(id: Int, nombre: String) {
        val bundle = Bundle().apply {
            putInt("playlistId", id)
            putString("playlistName", nombre)
        }
        findNavController().navigate(
            R.id.action_playlistsFragment_to_playlistSongsFragment,
            bundle
        )
    }
}
