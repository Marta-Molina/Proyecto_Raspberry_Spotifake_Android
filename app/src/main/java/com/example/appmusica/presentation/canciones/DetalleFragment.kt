package com.example.appmusica.presentation.canciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.appmusica.databinding.FragmentDetalleBinding
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetalleFragment : Fragment() {

    private var _binding: FragmentDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt("pos") ?: return

        val cancion = viewModel.getCancion(position)

        binding.txtNombre.text = cancion.nombre
        binding.txtArtista.text = cancion.artista
        binding.txtAlbum.text = cancion.album
        binding.txtDuracion.text = cancion.duracion

        Glide.with(requireContext())
            .load(cancion.imagen)
            .centerCrop()
            .into(binding.imgCancion)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
