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
import com.example.appmusica.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController

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
            delete = { pos -> 
                val cancion = adapter.getCancion(pos)
                if (cancion != null) viewModel.deleteCancion(cancion.id)
            },
            update = { pos -> openEdit(pos) },
            like = { pos ->
                adapter.getCancion(pos)?.let { viewModel.toggleLike(it) }
            },
            addToList = { pos ->
                // Por ahora no implementamos el diálogo aquí si no es necesario
            },
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
        (activity as? com.example.appmusica.presentation.MainActivity)?.expandPlayer(pos)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
