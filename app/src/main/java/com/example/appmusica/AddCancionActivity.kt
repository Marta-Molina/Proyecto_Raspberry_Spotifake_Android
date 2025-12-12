package com.example.appmusica

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmusica.databinding.ActivityAddCancionBinding
import com.example.appmusica.models.Cancion
import com.example.appmusica.objects_models.Repository

class AddCancionActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddCancionBinding

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

            if (nombre.isNotEmpty() && artista.isNotEmpty() && album.isNotEmpty() && duracion.isNotEmpty() && imagen.isNotEmpty()) {
                val nuevaCancion = Cancion(nombre, artista, album, duracion, imagen)
                Repository.listCanciones.add(nuevaCancion)
                Toast.makeText(this, "Canción agregada", Toast.LENGTH_SHORT).show()
                finish()  // Regresa a la MainActivity
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}