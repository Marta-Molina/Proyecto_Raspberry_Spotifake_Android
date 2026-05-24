package com.example.appmusica.presentation.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.databinding.FragmentNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SocialViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationAdapter = NotificationAdapter()
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }

        binding.btnClearNotifications.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Borrar notificaciones")
                .setMessage("¿Quieres borrar todas tus notificaciones?")
                .setPositiveButton("Borrar") { _, _ ->
                    viewModel.clearNotifications()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            binding.txtNoNotifications.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
            notificationAdapter.update(notifications)
        }

        viewModel.loadSocialData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
