package com.example.appmusica

import android.content.Intent
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

            // Verificar que los campos no estén vacíos
            if (nombre.isNotEmpty() && artista.isNotEmpty() && album.isNotEmpty() && duracion.isNotEmpty() && imagen.isNotEmpty()) {
                val nuevaCancion = Cancion(nombre, artista, album, duracion, imagen)
                // Agregar la nueva canción al repositorio
                Repository.listCanciones.add(nuevaCancion)
                Toast.makeText(this, "Canción agregada", Toast.LENGTH_SHORT).show()

                // Devolver el resultado a MainActivity
                val intent = Intent().apply {
                    putExtra("nuevaCancion", nuevaCancion) // Pasar la canción agregada
                }
                setResult(RESULT_OK, intent) // Indicar que fue exitoso
                finish()  // Cerrar la actividad
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}






