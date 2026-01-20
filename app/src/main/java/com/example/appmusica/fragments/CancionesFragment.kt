package com.example.appmusica.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
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

        controller = Controller(requireContext())
        controller.setAdapter(binding.recyclerCanciones)

        binding.recyclerCanciones.layoutManager =
            LinearLayoutManager(requireContext())

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddCancionActivity::class.java))
        }
    }
}
