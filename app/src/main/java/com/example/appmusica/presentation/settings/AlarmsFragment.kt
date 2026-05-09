package com.example.appmusica.presentation.settings

import android.content.Intent
import android.net.Uri
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
    private lateinit var alarmScheduler: com.example.appmusica.util.AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmScheduler = com.example.appmusica.util.AlarmScheduler(requireContext())
    }

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
            checkExactAlarmPermission {
                showAddAlarmDialog()
            }
        }

        viewModel.loadAlarms()
    }

    private fun checkExactAlarmPermission(onGranted: () -> Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Permiso necesario")
                    .setMessage("Para que las alarmas funcionen correctamente, Spotifake necesita permiso para programar alarmas exactas.")
                    .setPositiveButton("Configurar") { _, _ ->
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", requireContext().packageName, null)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                onGranted()
            }
        } else {
            onGranted()
        }
    }

    private fun setupRecyclerView() {
        adapter = AlarmsAdapter(
            onToggle = { alarm, isActive ->
                val updatedAlarm = alarm.copy(activo = isActive)
                viewModel.updateAlarm(alarm.id, updatedAlarm)
                
                if (isActive) {
                    checkExactAlarmPermission {
                        val song = cancionesViewModel.canciones.value?.find { it.id == alarm.cancionId }
                        alarmScheduler.schedule(updatedAlarm, song?.urlAudio)
                    }
                } else {
                    alarmScheduler.cancel(alarm.id)
                }
            },
            onDelete = { alarm ->
                viewModel.deleteAlarm(alarm.id)
                alarmScheduler.cancel(alarm.id)
            }
        )
        binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlarms.adapter = adapter
    }

    private fun observeAlarms() {
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms)
            // Opcionalmente: Verificar que todos los activos estén programados
            alarms.forEach { alarm ->
                if (alarm.activo) {
                    val song = cancionesViewModel.canciones.value?.find { it.id == alarm.cancionId }
                    alarmScheduler.schedule(alarm, song?.urlAudio)
                }
            }
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
