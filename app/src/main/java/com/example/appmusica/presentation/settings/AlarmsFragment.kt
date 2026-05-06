package com.example.appmusica.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.databinding.FragmentAlarmsBinding
import com.example.appmusica.domain.model.Alarma
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmsFragment : Fragment() {

    private var _binding: FragmentAlarmsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
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
                viewModel.updateAlarm(alarm.id, alarm.copy(activa = isActive))
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
        // Implementación simplificada de selector de hora y canción
        Toast.makeText(context, "Selector de alarma - En desarrollo", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
