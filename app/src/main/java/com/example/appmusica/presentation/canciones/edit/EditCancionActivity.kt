package com.example.appmusica.presentation.canciones.edit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.appmusica.databinding.ActivityEditCancionBinding
import com.example.appmusica.domain.model.Cancion
import dagger.hilt.android.AndroidEntryPoint
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel

@AndroidEntryPoint
class EditCancionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCancionBinding
    private val viewModel: CancionesViewModel by viewModels()

    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCancionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        position = intent.getIntExtra("pos", -1)
        if (position == -1) {
            finish()
            return
        }

        val cancion = viewModel.getCancion(position)

        binding.editTextNombre.setText(cancion.nombre)
        binding.editTextArtista.setText(cancion.artista)
        binding.editTextAlbum.setText(cancion.album)
        binding.editTextDuracion.setText(cancion.duracion)
        binding.editTextImagen.setText(cancion.imagen)

        binding.btnUpdateCancion.setOnClickListener {
            val nombre = binding.editTextNombre.text.toString()
            val artista = binding.editTextArtista.text.toString()
            val album = binding.editTextAlbum.text.toString()
            val duracion = binding.editTextDuracion.text.toString()
            val imagen = binding.editTextImagen.text.toString()

            if (nombre.isNotEmpty() &&
                artista.isNotEmpty() &&
                album.isNotEmpty() &&
                duracion.isNotEmpty() &&
                imagen.isNotEmpty()
            ) {
                val cancionActualizada = Cancion(
                    nombre = nombre,
                    artista = artista,
                    album = album,
                    duracion = duracion,
                    imagen = imagen
                )

                viewModel.updateCancion(position, cancionActualizada)

                Toast.makeText(this, "Canción actualizada", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}