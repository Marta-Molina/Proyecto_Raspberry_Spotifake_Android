package com.example.appmusica.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentDetalleBinding
import com.example.appmusica.objects_models.Repository

class DetalleFragment : Fragment(R.layout.fragment_detalle) {

    private lateinit var binding: FragmentDetalleBinding
    private val args: DetalleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleBinding.bind(view)

        val cancion = Repository.listCanciones[args.position]

        binding.txtNombre.text = cancion.nombre
        binding.txtArtista.text = cancion.artista
        binding.txtAlbum.text = cancion.album
        binding.txtDuracion.text = cancion.duracion

        Glide.with(requireContext())
            .load(cancion.imagen)
            .into(binding.imgCancion)
    }
}

