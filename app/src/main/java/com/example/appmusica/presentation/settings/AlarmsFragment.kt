package com.example.appmusica.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
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
                        alarmScheduler.schedule(updatedAlarm, song?.urlAudio, song?.nombre, song?.urlPortada)
                    }
                } else {
                    alarmScheduler.cancel(alarm.id)
                }
            },
            onEdit = { alarm ->
                showAddAlarmDialog(alarm)
            },
            onDelete = { alarm ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Alarma")
                    .setMessage("¿Estás seguro de que quieres eliminar esta alarma?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.deleteAlarm(alarm.id)
                        alarmScheduler.cancel(alarm.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            getSong = { cancionId ->
                cancionesViewModel.canciones.value?.find { it.id == cancionId }
            }
        )
        binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlarms.adapter = adapter
    }

    private fun observeAlarms() {
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms)
            // Verificar que el primero activo esté en la notificación scheduled
            val nextAlarm = alarms.find { it.activo }
            if (nextAlarm != null) {
                val song = cancionesViewModel.canciones.value?.find { it.id == nextAlarm.cancionId }
                alarmScheduler.schedule(nextAlarm, song?.urlAudio, song?.nombre, song?.urlPortada)
            } else {
                alarmScheduler.cancel(-1) // Escondemos notificación
            }
        }
    }

    private fun showAddAlarmDialog(existingAlarm: Alarma? = null) {
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
            .setTitle(if (existingAlarm == null) "Nueva Alarma" else "Editar Alarma")
            .setView(dialogView)
            .setPositiveButton(if (existingAlarm == null) "Crear" else "Guardar") { _, _ ->
                val hour = if (Build.VERSION.SDK_INT >= 23) timePicker.hour else timePicker.currentHour
                val minute = if (Build.VERSION.SDK_INT >= 23) timePicker.minute else timePicker.currentMinute
                selectedTime = String.format("%02d:%02d", hour, minute)
                selectedSongId = songs[spinnerSong.selectedItemPosition].id
                
                if (existingAlarm == null) {
                    val newAlarm = Alarma(
                        id = 0,
                        userId = 0L, 
                        nombre = "Alarma",
                        hora = selectedTime,
                        cancionId = selectedSongId,
                        activo = true
                    )
                    viewModel.createAlarm(newAlarm)
                } else {
                    val updatedAlarm = existingAlarm.copy(
                        hora = selectedTime,
                        cancionId = selectedSongId
                    )
                    viewModel.updateAlarm(existingAlarm.id, updatedAlarm)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
