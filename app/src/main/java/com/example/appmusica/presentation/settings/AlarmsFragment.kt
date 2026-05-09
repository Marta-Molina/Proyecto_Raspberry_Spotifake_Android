package com.example.appmusica.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentAlarmsBinding
import com.example.appmusica.domain.model.Alarma
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmsFragment : Fragment() {

    private var _binding: FragmentAlarmsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private val cancionesViewModel: com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel by activityViewModels()
    private lateinit var adapter: AlarmsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeAlarms()
        
        binding.btnAddAlarm.setOnClickListener {
            showAddAlarmDialog()
        }

        viewModel.loadAlarms()
    }

    private fun setupRecyclerView() {
        adapter = AlarmsAdapter(
            onToggle = { alarm, isActive ->
                viewModel.updateAlarm(alarm.id, alarm.copy(activo = isActive))
            },
            onDelete = { alarm ->
                viewModel.deleteAlarm(alarm.id)
            }
        )
        binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlarms.adapter = adapter
    }

    private fun observeAlarms() {
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms)
        }
    }

    private fun showAddAlarmDialog() {
        val songs = cancionesViewModel.canciones.value ?: emptyList()
        if (songs.isEmpty()) {
            Toast.makeText(context, "No hay canciones disponibles para la alarma", Toast.LENGTH_SHORT).show()
            return
        }

        val songNames = songs.map { it.nombre }.toTypedArray()
        var selectedSongId = songs[0].id
        var selectedTime = "08:00"

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_alarm, null)
        val timePicker = dialogView.findViewById<android.widget.TimePicker>(R.id.timePicker)
        val spinnerSong = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerAlarmSong)

        val spinnerAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, songNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSong.adapter = spinnerAdapter

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Nueva Alarma")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val hour = timePicker.hour
                val minute = timePicker.minute
                selectedTime = String.format("%02d:%02d", hour, minute)
                selectedSongId = songs[spinnerSong.selectedItemPosition].id
                
                android.util.Log.d("AlarmsFragment", "Creating alarm: $selectedTime for song $selectedSongId")
                
                val newAlarm = Alarma(
                    id = 0,
                    userId = 0L, 
                    nombre = "Alarma",
                    hora = selectedTime,
                    cancionId = selectedSongId,
                    activo = true
                )
                viewModel.createAlarm(newAlarm)
                Toast.makeText(context, "Creando alarma...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
