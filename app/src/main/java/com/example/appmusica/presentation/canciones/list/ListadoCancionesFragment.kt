package com.example.appmusica.presentation.canciones.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.databinding.FragmentListadoCancionesBinding
import com.example.appmusica.presentation.canciones.adapter.AdapterCancion
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.presentation.canciones.edit.EditCancionActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListadoCancionesFragment : Fragment() {

    private var _binding: FragmentListadoCancionesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by viewModels()
    private lateinit var adapter: AdapterCancion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListadoCancionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdapterCancion(
            mutableListOf(),
            delete = { pos -> viewModel.deleteCancion(pos) },
            update = { pos -> openEdit(pos) },
            onItemClick = { pos -> openDetalle(pos) }
        )

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.canciones.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
        }

        viewModel.loadCanciones()
    }

    private fun openEdit(pos: Int) {
        val intent = Intent(requireContext(), EditCancionActivity::class.java)
        intent.putExtra("pos", pos)
        startActivity(intent)
    }

    private fun openDetalle(pos: Int) {
        // Si no tienes pantalla detalle aún, puedes dejarlo vacío
        // o mostrar un Toast
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
