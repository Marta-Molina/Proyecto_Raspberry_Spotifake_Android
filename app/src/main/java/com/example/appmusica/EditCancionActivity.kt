package com.example.appmusica

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appmusica.databinding.ActivityEditCancionBinding
import com.example.appmusica.models.Cancion
import com.example.appmusica.objects_models.Repository

class EditCancionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCancionBinding
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCancionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        position = intent.getIntExtra("pos", -1)
        if (position == -1) finish()

        val cancion = Repository.listCanciones[position]

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

            if (nombre.isNotEmpty() && artista.isNotEmpty() && album.isNotEmpty() && duracion.isNotEmpty() && imagen.isNotEmpty()) {
                val cancionActualizada = Cancion(nombre, artista, album, duracion, imagen)
                Repository.listCanciones[position] = cancionActualizada
                Toast.makeText(this, "Canción actualizada", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
