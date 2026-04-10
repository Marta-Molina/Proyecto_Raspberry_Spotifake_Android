package com.example.appmusica.presentation.social

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.databinding.ItemFriendRequestBinding
import com.example.appmusica.domain.model.SolicitudAmistad

class FriendRequestAdapter(
    private val onAccept: (SolicitudAmistad) -> Unit,
    private val onReject: (SolicitudAmistad) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {
    private var requests: List<SolicitudAmistad> = emptyList()

    fun update(newList: List<SolicitudAmistad>) {
        requests = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(requests[position])
    override fun getItemCount() = requests.size

    inner class ViewHolder(private val binding: ItemFriendRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: SolicitudAmistad) {
            binding.txtRequesterName.text = "Usuario ${request.remitenteId}" // We'd ideally fetch the name
            binding.btnAccept.setOnClickListener { onAccept(request) }
            binding.btnReject.setOnClickListener { onReject(request) }
        }
    }
}
