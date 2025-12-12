package com.example.appmusica

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmusica.databinding.ActivityEditCancionBinding
import com.example.appmusica.models.Cancion
import com.example.appmusica.objects_models.Repository

// EditCancionActivity.kt
class EditCancionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCancionBinding
    private var cancionOriginal: Cancion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCancionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombre = intent.getStringExtra("nombre")
        val artista = intent.getStringExtra("artista")
        val album = intent.getStringExtra("album")
        val duracion = intent.getStringExtra("duracion")
        val imagen = intent.getStringExtra("imagen")

        binding.editTextNombre.setText(nombre)
        binding.editTextArtista.setText(artista)
        binding.editTextAlbum.setText(album)
        binding.editTextDuracion.setText(duracion)
        binding.editTextImagen.setText(imagen)

        // Guardar la canción original para poder actualizarla
        cancionOriginal = Cancion(nombre!!, artista!!, album!!, duracion!!, imagen!!)

        binding.btnUpdateCancion.setOnClickListener {
            val nuevoNombre = binding.editTextNombre.text.toString()
            val nuevoArtista = binding.editTextArtista.text.toString()
            val nuevoAlbum = binding.editTextAlbum.text.toString()
            val nuevaDuracion = binding.editTextDuracion.text.toString()
            val nuevaImagen = binding.editTextImagen.text.toString()

            // Validar campos
            if (nuevoNombre.isNotEmpty() && nuevoArtista.isNotEmpty() && nuevoAlbum.isNotEmpty() && nuevaDuracion.isNotEmpty() && nuevaImagen.isNotEmpty()) {
                // Crear una nueva canción con los datos actualizados
                val cancionActualizada = Cancion(nuevoNombre, nuevoArtista, nuevoAlbum, nuevaDuracion, nuevaImagen)

                // Devolver los datos a MainActivity
                val intent = Intent().apply {
                    putExtra("nombre", nuevoNombre)
                    putExtra("artista", nuevoArtista)
                    putExtra("album", nuevoAlbum)
                    putExtra("duracion", nuevaDuracion)
                    putExtra("imagen", nuevaImagen)
                }

                setResult(RESULT_OK, intent) // Indicar que fue exitoso
                finish() // Cerrar la actividad
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

