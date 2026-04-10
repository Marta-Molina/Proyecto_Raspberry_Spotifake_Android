package com.example.appmusica.presentation.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appmusica.databinding.FragmentSocialBinding
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.domain.model.SolicitudAmistad
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SocialViewModel by viewModels()

    private lateinit var searchAdapter: UserSearchAdapter
    private lateinit var requestAdapter: FriendRequestAdapter
    private lateinit var friendsAdapter: FriendSearchAdapter // Simple ID/Name list

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearch()
        setupLists()
        observeViewModel()

        viewModel.loadSocialData()
    }

    private fun setupSearch() {
        binding.etSearchUser.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            if (query.length >= 3) {
                viewModel.searchUsers(query)
                binding.rvUserSearch.visibility = View.VISIBLE
            } else {
                binding.rvUserSearch.visibility = View.GONE
            }
        }
    }

    private fun setupLists() {
        searchAdapter = UserSearchAdapter { user ->
            viewModel.sendFriendRequest(user.id)
            Toast.makeText(requireContext(), "Solicitud enviada a ${user.username}", Toast.LENGTH_SHORT).show()
        }
        binding.rvUserSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        requestAdapter = FriendRequestAdapter(
            onAccept = { req -> viewModel.acceptFriend(req.id) },
            onReject = { req -> /* TODO: logic for reject */ }
        )
        binding.rvRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = requestAdapter
        }

        // Using a simple list for friends for now
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            // Update friends text or minimal list
        }

        viewModel.pendingRequests.observe(viewLifecycleOwner) { requests ->
            requestAdapter.update(requests)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.update(results)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
