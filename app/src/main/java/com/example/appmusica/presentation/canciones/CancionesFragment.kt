package com.example.appmusica.presentation.canciones

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentCancionesBinding
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.presentation.canciones.add.AddCancionActivity
import com.example.appmusica.presentation.canciones.edit.EditCancionActivity
import com.example.appmusica.presentation.playlists.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CancionesFragment : Fragment(R.layout.fragment_canciones) {

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
                viewModel.getCancion(pos)?.let { viewModel.toggleLike(it) }
            },
            addToList = { pos ->
                mostrarDialogoListas(viewModel.getCancion(pos)?.id ?: -1)
            },
            onItemClick = { pos -> navegarADetalle(pos) }
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
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
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
        playlistViewModel.loadPlaylists()
        
        playlistViewModel.playlists.observe(viewLifecycleOwner) { _ ->
            // Just observing to keep it loaded
        }
    }

    private fun mostrarDialogoListas(cancionId: Int) {
        val playlists = playlistViewModel.playlists.value
        if (playlists == null || playlists.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "No tienes listas creadas", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = playlists.map { it.nombre }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar Lista")
            .setItems(nombres) { _, which ->
                val playlistId = playlists[which].id
                playlistViewModel.addSongToPlaylist(playlistId, cancionId)
                android.widget.Toast.makeText(requireContext(), "Canción añadida a ${playlists[which].nombre}", android.widget.Toast.LENGTH_SHORT).show()
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
