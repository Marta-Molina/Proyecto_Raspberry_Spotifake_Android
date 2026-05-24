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
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentSocialBinding
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.domain.model.SolicitudAmistad
import com.example.appmusica.presentation.playlists.PlaylistAdapter
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SocialViewModel by viewModels()

    private lateinit var searchAdapter: UserSearchAdapter
    private lateinit var requestAdapter: FriendRequestAdapter
    private lateinit var sentRequestAdapter: SentRequestAdapter
    private lateinit var friendsAdapter: FriendAdapter

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

        binding.swipeRefreshSocial.setOnRefreshListener {
            viewModel.loadSocialData()
        }
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
        searchAdapter = UserSearchAdapter(
            onAddClick = { user ->
                viewModel.sendFriendRequest(user.id)
                Toast.makeText(requireContext(), "Solicitud enviada a ${user.username}", Toast.LENGTH_SHORT).show()
            },
            isSentCheck = { userId ->
                viewModel.isRequestSent(userId)
            }
        )
        binding.rvUserSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        requestAdapter = FriendRequestAdapter(
            onAccept = { req -> viewModel.acceptFriend(req.id) },
            onReject = { req -> viewModel.rejectFriend(req.id) }
        )
        binding.rvRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = requestAdapter
        }

        sentRequestAdapter = SentRequestAdapter { req ->
            viewModel.rejectFriend(req.id) // Use reject/delete endpoint to cancel sent request
        }
        binding.rvSentRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sentRequestAdapter
        }

        friendsAdapter = FriendAdapter(
            onClick = { user ->
                // Fetch and show shared playlists
                viewModel.loadSharedPlaylists(user.id)
                mostrarDialogoPlaylistsCompartidas(user.username)
            },
            onDelete = { user ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar amigo")
                    .setMessage("¿Estás seguro de que quieres eliminar a ${user.username} de tus amigos?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.deleteFriend(user.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            friendsAdapter.update(friends)
            binding.txtNoFriends.visibility = if (friends.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.pendingRequests.observe(viewLifecycleOwner) { requests ->
            requestAdapter.update(requests)
            binding.txtNoRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.sentRequests.observe(viewLifecycleOwner) { requests ->
            sentRequestAdapter.update(requests)
            binding.txtNoSentRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.update(results)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshSocial.isRefreshing = isLoading
        }
    }

    private fun mostrarDialogoPlaylistsCompartidas(friendName: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_shared_playlists, null)
        val rvShared = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSharedPlaylists)
        val txtNoShared = dialogView.findViewById<android.widget.TextView>(R.id.txtNoSharedPlaylists)

        lateinit var adapter: PlaylistAdapter
        adapter = PlaylistAdapter(
            list = mutableListOf(),
            onClick = { position ->
                adapter.getPlaylist(position)?.let { playlist ->
                    val bundle = Bundle().apply {
                        putInt("playlistId", playlist.id.toInt())
                        putString("playlistName", playlist.nombre)
                    }
                    findNavController().navigate(R.id.playlistSongsFragment, bundle)
                }
            },
            onDelete = { /* Shared playlists shouldn't be deleted from here */ },
            onEdit = { /* Shared playlists shouldn't be edited from here */ },
            onShare = { /* No reshare for now */ }
        )

        rvShared.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Listas compartidas por $friendName")
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .create()

        viewModel.sharedPlaylists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                txtNoShared.visibility = View.VISIBLE
                rvShared.visibility = View.GONE
            } else {
                txtNoShared.visibility = View.GONE
                rvShared.visibility = View.VISIBLE
                adapter.updateList(playlists)
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
