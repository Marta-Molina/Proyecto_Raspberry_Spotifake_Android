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

            // Schedule ALL active alarms individually
            alarms.filter { it.activo }.forEach { alarm ->
                val song = cancionesViewModel.canciones.value?.find { it.id == alarm.cancionId }
                alarmScheduler.schedule(alarm, song?.urlAudio, song?.nombre, song?.urlPortada)
            }

            // For the notification, show the next upcoming one
            val sortedActiveAlarms = alarms.filter { it.activo }.sortedBy { it.hora }
            val nextAlarm = sortedActiveAlarms.firstOrNull {
                val alarmTime = it.hora.split(":")
                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, alarmTime[0].toInt())
                    set(java.util.Calendar.MINUTE, alarmTime[1].toInt())
                    set(java.util.Calendar.SECOND, 0)
                }
                calendar.timeInMillis > System.currentTimeMillis()
            } ?: sortedActiveAlarms.firstOrNull()

            if (nextAlarm == null) {
                alarmScheduler.cancel(-1) // Hides notification
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

        timePicker.setIs24HourView(true)

        val cbMon = dialogView.findViewById<android.widget.CheckBox>(R.id.cbMon)
        val cbTue = dialogView.findViewById<android.widget.CheckBox>(R.id.cbTue)
        val cbWed = dialogView.findViewById<android.widget.CheckBox>(R.id.cbWed)
        val cbThu = dialogView.findViewById<android.widget.CheckBox>(R.id.cbThu)
        val cbFri = dialogView.findViewById<android.widget.CheckBox>(R.id.cbFri)
        val cbSat = dialogView.findViewById<android.widget.CheckBox>(R.id.cbSat)
        val cbSun = dialogView.findViewById<android.widget.CheckBox>(R.id.cbSun)
        val checkboxes = listOf(cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun)

        // Pre-fill existing data
        if (existingAlarm != null) {
            val parts = existingAlarm.hora.split(":")
            if (parts.size == 2) {
                if (Build.VERSION.SDK_INT >= 23) {
                    timePicker.hour = parts[0].toInt()
                    timePicker.minute = parts[1].toInt()
                } else {
                    timePicker.currentHour = parts[0].toInt()
                    timePicker.currentMinute = parts[1].toInt()
                }
            }

            existingAlarm.dias?.split(",")?.forEach { day ->
                val dayInt = day.toIntOrNull()
                if (dayInt != null && dayInt in 1..7) {
                    checkboxes[dayInt - 1].isChecked = true
                    checkboxes[dayInt - 1].setBackgroundColor(android.graphics.Color.parseColor("#1DB954"))
                }
            }

            val songIndex = songs.indexOfFirst { it.id == existingAlarm.cancionId }
            if (songIndex >= 0) spinnerSong.setSelection(songIndex)
        }

        // Add visual feedback to checkboxes
        checkboxes.forEachIndexed { index, cb ->
            cb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    cb.setBackgroundColor(android.graphics.Color.parseColor("#1DB954"))
                } else {
                    cb.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            }
        }

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

                val selectedDays = checkboxes.mapIndexedNotNull { index, cb ->
                    if (cb.isChecked) (index + 1).toString() else null
                }.joinToString(",")

                if (existingAlarm == null) {
                    val newAlarm = Alarma(
                        id = 0,
                        userId = 0L,
                        nombre = "Alarma",
                        hora = selectedTime,
                        cancionId = selectedSongId,
                        activo = true,
                        dias = selectedDays
                    )
                    viewModel.createAlarm(newAlarm)
                } else {
                    val updatedAlarm = existingAlarm.copy(
                        hora = selectedTime,
                        cancionId = selectedSongId,
                        dias = selectedDays
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
