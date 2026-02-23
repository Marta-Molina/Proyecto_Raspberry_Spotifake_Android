package com.example.appmusica.presentation.admin

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentManagePlaylistsBinding
import com.example.appmusica.presentation.playlists.PlaylistAdapter
import com.example.appmusica.presentation.playlists.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManagePlaylistsFragment : Fragment(R.layout.fragment_manage_playlists) {

    private lateinit var binding: FragmentManagePlaylistsBinding
    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var adapter: PlaylistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentManagePlaylistsBinding.bind(view)

        adapter = PlaylistAdapter(
            list = mutableListOf(),
            onDelete = { pos ->
                adapter.getPlaylist(pos)?.let { playlist ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Eliminar playlist")
                        .setMessage("¿Estás seguro de que quieres eliminar la lista '${playlist.nombre}'?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            viewModel.deletePlaylist(playlist.id)
                            Toast.makeText(context, "Playlist eliminada", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            },
            onEdit = { pos ->
                adapter.getPlaylist(pos)?.let { mostrarDialogoEditar(it.id, it.nombre, it.idUsuario) }
            },
            onClick = { pos ->
                adapter.getPlaylist(pos)?.let { navegarACanciones(it.id, it.nombre) }
            }
        )

        binding.rvManagePlaylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ManagePlaylistsFragment.adapter
        }

        viewModel.playlists.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)
        }

        viewModel.loadAllPlaylists()
    }

    private fun mostrarDialogoEditar(id: Int, nombreActual: String, userId: Int) {
        val editText = EditText(requireContext())
        editText.setText(nombreActual)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Lista")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString()
                if (nuevoNombre.isNotBlank()) {
                    viewModel.updatePlaylist(id, nuevoNombre, userId)
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
            R.id.action_managePlaylistsFragment_to_playlistSongsFragment,
            bundle
        )
    }
}
