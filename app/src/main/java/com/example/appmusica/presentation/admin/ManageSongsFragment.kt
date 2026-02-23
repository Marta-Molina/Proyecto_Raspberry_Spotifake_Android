package com.example.appmusica.presentation.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.R
import com.example.appmusica.presentation.canciones.add.AddCancionActivity
import com.example.appmusica.presentation.canciones.edit.EditCancionActivity
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageSongsFragment : Fragment() {

    private val viewModel: CancionesViewModel by viewModels()
    private lateinit var songAdapter: ManageSongsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_songs, container, false)
        
        val rvSongs = view.findViewById<RecyclerView>(R.id.rvManageSongs)
        rvSongs.layoutManager = LinearLayoutManager(context)
        
        songAdapter = ManageSongsAdapter(emptyList(),
            onEdit = { song -> 
                val intent = Intent(requireContext(), EditCancionActivity::class.java)
                // Usamos la posición en la lista actual. 
                // Nota: EditCancionActivity actual requiere "pos" y lo busca en el viewModel.
                // Esto es un poco frágil si la lista cambia, pero es como está implementado EditCancionActivity.
                val position = viewModel.canciones.value?.indexOf(song) ?: -1
                if (position != -1) {
                    intent.putExtra("pos", position)
                    startActivity(intent)
                }
            },
            onDelete = { song -> showDeleteDialog(song.id) }
        )
        rvSongs.adapter = songAdapter

        view.findViewById<FloatingActionButton>(R.id.fabAddSong).setOnClickListener {
            startActivity(Intent(requireContext(), AddCancionActivity::class.java))
        }

        viewModel.canciones.observe(viewLifecycleOwner) { songs ->
            songAdapter.updateSongs(songs)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCanciones()
    }

    private fun showDeleteDialog(songId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar canción")
            .setMessage("¿Estás seguro de que quieres eliminar esta canción?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteCancion(songId)
                Toast.makeText(context, "Canción eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
