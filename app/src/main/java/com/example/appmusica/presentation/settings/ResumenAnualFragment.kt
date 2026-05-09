package com.example.appmusica.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.appmusica.databinding.FragmentResumenAnualBinding
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.util.FormatUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class ResumenAnualFragment : Fragment() {

    private var _binding: FragmentResumenAnualBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CancionesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenAnualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.txtResumenTitle.text = "Tu Resumen del $currentYear"

        viewModel.getStats(currentYear)

        viewModel.stats.observe(viewLifecycleOwner) { resumen ->
            if (resumen != null) {
                binding.txtTotalTime.text = "${resumen.totalTimeSeconds / 60} minutos escuchados"
                binding.txtTotalReproducciones.text = "${FormatUtils.formatCount(resumen.totalReproductions)} canciones reproducidas"
                
                // Aquí se podrían cargar los detalles de las canciones y artistas top usando sus IDs
                // Por ahora mostramos los IDs o un mensaje simple
                binding.txtTopSongs.text = "Tus canciones favoritas están listas para sonar"
                binding.txtTopArtists.text = "Tus artistas más escuchados te esperan"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
