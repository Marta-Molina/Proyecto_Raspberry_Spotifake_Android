package com.example.appmusica.presentation.canciones

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.AddCancionActivity
import com.example.appmusica.Controller
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentCancionesBinding

class CancionesFragment : Fragment(R.layout.fragment_canciones) {

    private lateinit var binding: FragmentCancionesBinding
    private lateinit var controller: Controller

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCancionesBinding.bind(view)

        // Inicializamos el controller y el RecyclerView
        controller = Controller(requireContext()) { position ->
            navegarADetalle(position)
        }

        binding.recyclerCanciones.layoutManager =
            LinearLayoutManager(requireContext())

        controller.setAdapter(binding.recyclerCanciones)

        // FAB para añadir canción
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddCancionActivity::class.java))
        }
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