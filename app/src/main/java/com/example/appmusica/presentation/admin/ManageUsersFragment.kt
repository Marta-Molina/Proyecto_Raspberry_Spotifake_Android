package com.example.appmusica.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.appmusica.R
import com.example.appmusica.data.remote.request.UserRequest
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.retrofit.ApiCancionesService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ManageUsersFragment : Fragment() {

    @Inject
    lateinit var apiService: ApiCancionesService

    private lateinit var userAdapter: UserAdapter
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_users, container, false)
        
        val rvUsers = view.findViewById<RecyclerView>(R.id.rvUsers)
        rvUsers.layoutManager = LinearLayoutManager(context)
        
        userAdapter = UserAdapter(emptyList(), 
            onPromote = { user -> promoteUser(user) },
            onDelete = { user -> deleteUser(user) },
            onClick = { user -> 
                val bundle = Bundle().apply {
                    putSerializable("user", user)
                }
                findNavController().navigate(
                    R.id.action_manageUsersFragment_to_userDetailFragment,
                    bundle
                )
            }
        )
        rvUsers.adapter = userAdapter

        loadUsers()
        
        return view
    }

    private fun loadUsers() {
        scope.launch {
            try {
                val response = apiService.getUsuarios()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userAdapter.updateUsers(response.body() ?: emptyList())
                    } else {
                        Toast.makeText(context, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun promoteUser(user: UserResponse) {
        scope.launch {
            try {
                // Preparamos el request con admin = true
                val updateRequest = UserRequest(
                    username = user.username,
                    correo = user.correo,
                    pass = "", // La API no debería cambiar la pass si viene vacía o nula en este endpoint de admin
                    admin = true,
                    premium = user.premium == 1
                )
                val response = apiService.updateUsuario(user.id, updateRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "${user.username} ahora es administrador", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(context, "Error al promocionar usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteUser(user: UserResponse) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Estás seguro de que quieres eliminar a ${user.username}?")
            .setPositiveButton("Eliminar") { _, _ ->
                scope.launch {
                    try {
                        val response = apiService.deleteUsuario(user.id)
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            } else {
                                Toast.makeText(context, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
