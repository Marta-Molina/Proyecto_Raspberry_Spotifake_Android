package com.example.appmusica.presentation.playlists

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
import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.databinding.FragmentPlaylistsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    @Inject
    lateinit var authManager: AuthManager

    private lateinit var binding: FragmentPlaylistsBinding
    private val viewModel: PlaylistViewModel by viewModels()
    private val socialViewModel: com.example.appmusica.presentation.social.SocialViewModel by viewModels()
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
                            viewModel.deletePlaylist(playlist.id, authManager.getUserId().toInt())
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            },
            onEdit = { pos ->
                adapter.getPlaylist(pos)?.let { mostrarDialogoEditar(it.id, it.nombre, it.idUsuario) }
            },
            onShare = { pos ->
                adapter.getPlaylist(pos)?.let { compartirPlaylist(it) }
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

        viewModel.loadPlaylists(authManager.getUserId().toInt())
        socialViewModel.loadSocialData()
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
                    viewModel.createPlaylist(nombre, authManager.getUserId().toInt())
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
            R.id.action_playlistsFragment_to_playlistSongsFragment,
            bundle
        )
    }

    private fun compartirPlaylist(playlist: com.example.appmusica.domain.model.Playlist) {
        val options = arrayOf("Compartir enlace externo", "Enviar a un amigo")

        AlertDialog.Builder(requireContext())
            .setTitle("Compartir '${playlist.nombre}'")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> compartirEnlaceExterno(playlist)
                    1 -> mostrarDialogoEnviarAFriend(playlist)
                }
            }
            .show()
    }

    private fun compartirEnlaceExterno(playlist: com.example.appmusica.domain.model.Playlist) {
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Spotifake Playlist: ${playlist.nombre}")
            val shareUrl = "https://spotifake.app/playlist/${playlist.id}"
            putExtra(android.content.Intent.EXTRA_TEXT, "¡Escucha mi playlist '${playlist.nombre}' en Spotifake!\n$shareUrl")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Compartir via"))
    }

    private fun mostrarDialogoEnviarAFriend(playlist: com.example.appmusica.domain.model.Playlist) {
        val friends = socialViewModel.friends.value ?: emptyList()
        if (friends.isEmpty()) {
            Toast.makeText(requireContext(), "No tienes amigos agregados aún", Toast.LENGTH_SHORT).show()
            return
        }

        val friendNames = friends.map { it.username }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Enviar a...")
            .setItems(friendNames) { _, which ->
                val selectedFriend = friends[which]
                socialViewModel.sharePlaylist(playlist.id.toLong(), selectedFriend.id ?: 0L)
                Toast.makeText(requireContext(), "¡Playlist enviada a ${selectedFriend.username}!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
