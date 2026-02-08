package com.example.appmusica.presentation.canciones

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentCancionesBinding
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.presentation.canciones.add.AddCancionActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CancionesFragment : Fragment(R.layout.fragment_canciones) {

    private lateinit var binding: FragmentCancionesBinding
    private val viewModel: CancionesViewModel by viewModels()
    private lateinit var adapter: AdapterCancion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCancionesBinding.bind(view)

        adapter = AdapterCancion(
            list = mutableListOf(),
            delete = { pos -> viewModel.deleteCancion(pos) },
            update = { pos ->
                viewModel.editCancion(requireContext(), pos)
            },
            onItemClick = { pos -> navegarADetalle(pos) }
        )

        binding.recyclerCanciones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CancionesFragment.adapter
        }

        viewModel.canciones.observe(viewLifecycleOwner) { lista ->
            adapter.updateList(lista)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddCancionActivity::class.java))
        }

        viewModel.loadCanciones()
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
