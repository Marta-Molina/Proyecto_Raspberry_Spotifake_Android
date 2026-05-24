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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        // For now, we reuse pending requests as notifications
        viewModel.pendingRequests.observe(viewLifecycleOwner) { requests ->
            binding.txtNoNotifications.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
            // TODO: Create a NotificationAdapter
        }
        
        viewModel.loadSocialData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
