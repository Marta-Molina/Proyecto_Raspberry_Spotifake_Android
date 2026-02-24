package com.example.appmusica.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.R
import com.example.appmusica.domain.model.Genero
import com.example.appmusica.retrofit.ApiCancionesService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.appmusica.util.setClickAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ManageGenresFragment : Fragment() {

    @Inject
    lateinit var apiService: ApiCancionesService

    private lateinit var genreAdapter: GenreAdapter
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_genres, container, false)
        
        val rvGenres = view.findViewById<RecyclerView>(R.id.rvGenres)
        rvGenres.layoutManager = LinearLayoutManager(context)
        
        genreAdapter = GenreAdapter(emptyList(),
            onEdit = { genre -> showEditGenreDialog(genre) },
            onDelete = { genre -> showDeleteGenreDialog(genre) }
        )
        rvGenres.adapter = genreAdapter

        view.findViewById<FloatingActionButton>(R.id.fabAddGenre).let {
            it.setOnClickListener { showAddGenreDialog() }
            it.setClickAnimation()
        }

        loadGenres()
        
        return view
    }

    private fun loadGenres() {
        scope.launch {
            try {
                val response = apiService.getGeneros()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        genreAdapter.updateGenres(response.body() ?: emptyList())
                    } else {
                        Toast.makeText(context, "Error al cargar géneros", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAddGenreDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Nombre del género"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Añadir Género")
            .setView(editText)
            .setPositiveButton("Añadir") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    addGenre(name)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addGenre(name: String) {
        scope.launch {
            try {
                val response = apiService.addGenero(Genero(0, name))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Género añadido", Toast.LENGTH_SHORT).show()
                        loadGenres()
                    } else {
                        Toast.makeText(context, "Error al añadir género", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEditGenreDialog(genre: Genero) {
        val editText = EditText(requireContext())
        editText.setText(genre.nombre)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Género")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    updateGenre(genre.id, name)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateGenre(id: Int, name: String) {
        scope.launch {
            try {
                val response = apiService.updateGenero(id, Genero(id, name))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Género actualizado", Toast.LENGTH_SHORT).show()
                        loadGenres()
                    } else {
                        Toast.makeText(context, "Error al actualizar género", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteGenreDialog(genre: Genero) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Género")
            .setMessage("¿Estás seguro de que quieres eliminar '${genre.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteGenre(genre.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteGenre(id: Int) {
        scope.launch {
            try {
                val response = apiService.deleteGenero(id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Género eliminado", Toast.LENGTH_SHORT).show()
                        loadGenres()
                    } else {
                        Toast.makeText(context, "Error al eliminar género", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
