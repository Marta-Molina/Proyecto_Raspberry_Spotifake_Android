package com.example.appmusica.presentation.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.appmusica.R
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

        notificationAdapter = NotificationAdapter { notif ->
            viewModel.markNotificationAsRead(notif.id)

            when (notif.tipo) {
                "friend_request", "friend_accepted" -> {
                    findNavController().navigate(R.id.socialFragment)
                }
                "playlist_shared" -> {
                    notif.idReferencia?.let { playlistId ->
                        val bundle = Bundle().apply {
                            putInt("playlistId", playlistId)
                            putString("playlistName", "Playlist compartida")
                        }
                        findNavController().navigate(R.id.playlistSongsFragment, bundle)
                    } ?: run {
                        findNavController().navigate(R.id.socialFragment)
                    }
                }
                "new_release" -> {
                    // Could navigate to the song/album if idReferencia was set,
                    // for now just go to main songs
                    findNavController().navigate(R.id.cancionesFragment)
                }
            }
        }
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
