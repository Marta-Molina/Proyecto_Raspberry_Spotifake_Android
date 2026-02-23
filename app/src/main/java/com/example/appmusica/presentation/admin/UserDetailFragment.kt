package com.example.appmusica.presentation.admin

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.di.NetworkModule
import com.example.appmusica.presentation.playlists.PlaylistAdapter
import com.example.appmusica.presentation.playlists.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailFragment : Fragment(R.layout.fragment_user_detail) {

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter
    private var user: UserResponse? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = arguments?.getSerializable("user") as? UserResponse
        if (user == null) {
            findNavController().popBackStack()
            return
        }

        setupUserInfo(view)
        setupRecyclerView(view)
        observePlaylists()

        user?.let { playlistViewModel.loadUserPlaylists(it.id.toInt()) }
    }

    private fun setupUserInfo(view: View) {
        val ivPhoto = view.findViewById<ImageView>(R.id.ivUserDetailPhoto)
        val tvName = view.findViewById<TextView>(R.id.tvUserDetailName)
        val tvEmail = view.findViewById<TextView>(R.id.tvUserDetailEmail)
        val tvRole = view.findViewById<TextView>(R.id.tvUserDetailRole)

        user?.let {
            tvName.text = "${it.nombre} ${it.apellido1 ?: ""} ${it.apellido2 ?: ""}".trim()
            tvEmail.text = it.correo
            tvRole.text = if (it.admin) "Administrador" else "Usuario Estándar"
            
            val baseUrl = NetworkModule.BASE_URL.replace("/api/", "").removeSuffix("/")
            it.urlImagen?.let { url ->
                Glide.with(this)
                    .load(baseUrl + url)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .circleCrop()
                    .into(ivPhoto)
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val rvPlaylists = view.findViewById<RecyclerView>(R.id.rvUserPlaylists)
        rvPlaylists.layoutManager = LinearLayoutManager(context)

        playlistAdapter = PlaylistAdapter(
            list = mutableListOf(),
            onDelete = { pos ->
                playlistAdapter.getPlaylist(pos)?.let { playlist ->
                    showDeletePlaylistDialog(playlist.id)
                }
            },
            onEdit = { pos ->
                playlistAdapter.getPlaylist(pos)?.let { playlist ->
                    showEditPlaylistDialog(playlist.id, playlist.nombre, playlist.idUsuario)
                }
            },
            onClick = { pos ->
                // Opcional: navegar a las canciones de la playlist
                playlistAdapter.getPlaylist(pos)?.let { playlist ->
                    val bundle = Bundle().apply {
                        putInt("playlistId", playlist.id)
                        putString("playlistName", playlist.nombre)
                    }
                    findNavController().navigate(R.id.action_userDetailFragment_to_playlistSongsFragment, bundle)
                }
            }
        )
        rvPlaylists.adapter = playlistAdapter
    }

    private fun observePlaylists() {
        playlistViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.updateList(playlists)
        }
    }

    private fun showDeletePlaylistDialog(playlistId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Playlist")
            .setMessage("¿Estás seguro de que quieres eliminar esta lista?")
            .setPositiveButton("Eliminar") { _, _ ->
                user?.let { u ->
                    playlistViewModel.deletePlaylist(playlistId, u.id.toInt())
                }
                Toast.makeText(context, "Playlist eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditPlaylistDialog(id: Int, nombreActual: String, userId: Int) {
        val editText = EditText(requireContext())
        editText.setText(nombreActual)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Playlist")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString()
                if (nuevoNombre.isNotBlank()) {
                    playlistViewModel.updatePlaylist(id, nuevoNombre, userId)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
