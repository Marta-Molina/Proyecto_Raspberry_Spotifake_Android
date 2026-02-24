package com.example.appmusica.presentation.canciones

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.data.local.LikedSongsManager
import com.example.appmusica.databinding.FragmentCancionesBinding
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.presentation.canciones.add.AddCancionActivity
import com.example.appmusica.presentation.canciones.edit.EditCancionActivity
import com.example.appmusica.presentation.playlists.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class CancionesFragment : Fragment(R.layout.fragment_canciones) {

    @javax.inject.Inject
    lateinit var authManager: AuthManager

    @javax.inject.Inject
    lateinit var likedSongsManager: LikedSongsManager

    private lateinit var binding: FragmentCancionesBinding
    private val viewModel: CancionesViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var adapter: AdapterCancion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCancionesBinding.bind(view)

        adapter = AdapterCancion(
            list = mutableListOf(),
            delete = { pos -> viewModel.deleteCancion(viewModel.getCancion(pos)?.id ?: -1) },
            update = { pos ->
                val intent = Intent(requireContext(), EditCancionActivity::class.java)
                intent.putExtra("pos", pos)
                startActivity(intent)
            },
            like = { pos ->
                val cancion = viewModel.getCancion(pos)
                if (cancion != null) {
                    handleLike(cancion)
                }
            },
            addToList = { pos ->
                mostrarDialogoListas(viewModel.getCancion(pos)?.id ?: -1)
            },
            onItemClick = { pos -> navegarADetalle(pos) },
            isLiked = { cancionId -> likedSongsManager.isLiked(cancionId) }
        )

        binding.recyclerCanciones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CancionesFragment.adapter
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.loadCanciones(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.loadCanciones(newText)
                return true
            }
        })

        viewModel.generos.observe(viewLifecycleOwner) { generos ->
            val genreNames = mutableListOf("Todos los géneros")
            genreNames.addAll(generos.map { it.nombre })

            val adapterSpinner = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                genreNames
            )
            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenero.adapter = adapterSpinner
        }

        binding.spinnerGenero.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, idSelected: Long) {
                if (position == 0) {
                    viewModel.loadCanciones(generoId = 0)
                } else {
                    val genreId = viewModel.generos.value?.get(position - 1)?.id ?: 0
                    viewModel.loadCanciones(generoId = genreId)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        viewModel.canciones.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddCancionActivity::class.java))
        }

        viewModel.loadCanciones()
        playlistViewModel.loadPlaylists(authManager.getUserId().toInt())

        playlistViewModel.playlists.observe(viewLifecycleOwner) { _ ->
            // Just observing to keep it loaded
        }
    }

    // ── Like logic ──────────────────────────────────────────────────────────────

    private fun handleLike(cancion: Cancion) {
        if (likedSongsManager.isLiked(cancion.id)) {
            Toast.makeText(requireContext(), "¡Ya diste like a esta canción!", Toast.LENGTH_SHORT).show()
            return
        }
        // Mark as liked locally first so the star turns green immediately
        likedSongsManager.setLiked(cancion.id)
        // Persist the incremented like count to the backend
        viewModel.addLike(cancion)
        // Celebrate!
        showLikeConfetti()
    }

    private fun showLikeConfetti() {
        binding.konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 25f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(
                    android.graphics.Color.parseColor("#1DB954"), // Spotify green
                    android.graphics.Color.parseColor("#FFE137"), // Golden yellow
                    android.graphics.Color.parseColor("#FF5C5C"), // Coral red
                    android.graphics.Color.parseColor("#9B59B6"), // Purple
                    android.graphics.Color.parseColor("#3498DB")  // Blue
                ),
                shapes = listOf(Shape.Circle, Shape.Square),
                size = listOf(Size.SMALL, Size.LARGE),
                timeToLive = 2000L,
                emitter = Emitter(duration = 150, TimeUnit.MILLISECONDS).max(120),
                position = Position.Relative(0.5, 0.3)
            )
        )
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private fun mostrarDialogoListas(cancionId: Int) {
        val playlists = playlistViewModel.playlists.value
        if (playlists == null || playlists.isEmpty()) {
            Toast.makeText(requireContext(), "No tienes listas creadas", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = playlists.map { it.nombre }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar Lista")
            .setItems(nombres) { _, which ->
                val playlistId = playlists[which].id
                playlistViewModel.addSongToPlaylist(playlistId, cancionId)
                Toast.makeText(requireContext(), "Canción añadida a ${playlists[which].nombre}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navegarADetalle(position: Int) {
        val bundle = Bundle().apply {
            putInt("position", position)
        }
        findNavController().navigate(
            R.id.action_cancionesFragment_to_detalleFragment,
            bundle
        )
    }
}
