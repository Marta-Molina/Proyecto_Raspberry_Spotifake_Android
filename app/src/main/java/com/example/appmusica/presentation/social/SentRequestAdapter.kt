package com.example.appmusica.presentation.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.databinding.ItemFriendRequestSentBinding
import com.example.appmusica.domain.model.SolicitudAmistad
import com.example.appmusica.data.remote.response.UserResponse

class SentRequestAdapter(
    private val onDelete: (SolicitudAmistad) -> Unit
) : RecyclerView.Adapter<SentRequestAdapter.ViewHolder>() {
    private var requests: List<Pair<SolicitudAmistad, UserResponse?>> = emptyList()

    fun update(newList: List<Pair<SolicitudAmistad, UserResponse?>>) {
        requests = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendRequestSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(requests[position])
    override fun getItemCount() = requests.size

    inner class ViewHolder(private val binding: ItemFriendRequestSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<SolicitudAmistad, UserResponse?>) {
            val request = item.first
            val user = item.second

            binding.txtReceiverName.text = user?.username ?: "Usuario ${request.destinatarioId}"

            when (request.estado.lowercase()) {
                "rechazada" -> {
                    binding.txtStatus.text = "Rechazada"
                    binding.txtStatus.setTextColor(android.graphics.Color.RED)
                }
                "pendiente", "esperando" -> {
                    binding.txtStatus.text = "Pendiente"
                    binding.txtStatus.setTextColor(android.graphics.Color.GRAY)
                }
                "aceptada" -> {
                    binding.txtStatus.text = "Aceptada"
                    binding.txtStatus.setTextColor(android.graphics.Color.GREEN)
                }
                else -> {
                    binding.txtStatus.text = request.estado
                    binding.txtStatus.setTextColor(android.graphics.Color.GRAY)
                }
            }

            binding.btnDeleteRequest.setOnClickListener { onDelete(request) }
        }
    }
}
