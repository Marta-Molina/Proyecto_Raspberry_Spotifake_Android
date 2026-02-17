package com.example.appmusica.presentation.canciones.add

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.appmusica.databinding.ActivityAddCancionBinding
import com.example.appmusica.domain.model.Cancion
import dagger.hilt.android.AndroidEntryPoint
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel

@AndroidEntryPoint
class AddCancionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCancionBinding
    private val viewModel: CancionesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCancionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddCancion.setOnClickListener {
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
                val nuevaCancion = Cancion(
                    id = (0..1000).random(), // Generate dummy ID for now
                    nombre = nombre,
                    artista = artista,
                    album = album,
                    portadaUrl = imagen,
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" // Dummy audio
                )

                viewModel.addCancion(nuevaCancion)

                Toast.makeText(this, "Canción agregada", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}