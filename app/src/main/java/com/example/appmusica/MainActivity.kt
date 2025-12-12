package com.example.appmusica

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.databinding.ActivityMainBinding
import com.example.appmusica.models.Cancion

class MainActivity : AppCompatActivity() {

    private lateinit var controller: Controller
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = Controller(this) // Crear el controlador
        controller.setAdapter()
    }

    // Sobrescribir onActivityResult para manejar el resultado de la actividad de edición
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Verificar si el código de solicitud coincide con el que hemos definido
        if (requestCode == Controller.REQUEST_CODE_UPDATE && resultCode == RESULT_OK) {
            // Aquí debes actualizar la canción en la lista con los nuevos datos
            data?.let {
                val nombre = it.getStringExtra("nombre") ?: ""
                val artista = it.getStringExtra("artista") ?: ""
                val album = it.getStringExtra("album") ?: ""
                val duracion = it.getStringExtra("duracion") ?: ""
                val imagen = it.getStringExtra("imagen") ?: ""

                // Encuentra la canción que se editó y actualízala
                val index = controller.listCanciones.indexOfFirst { it.nombre == nombre }
                if (index != -1) {
                    controller.listCanciones[index] =
                        Cancion(nombre, artista, album, duracion, imagen)
                    controller.setAdapter() // Actualizar el RecyclerView
                }
            }
        }
    }
}
