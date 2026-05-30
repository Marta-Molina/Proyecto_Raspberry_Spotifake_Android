package com.example.appmusica.presentation.social

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemUserSearchBinding
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.di.NetworkModule

class UserSearchAdapter(
    private val onAddClick: (UserResponse) -> Unit,
    private val isSentCheck: (Long) -> Boolean
) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {
    private var users: List<UserResponse> = emptyList()

    fun update(newList: List<UserResponse>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(users[position])
    override fun getItemCount() = users.size

    inner class ViewHolder(private val binding: ItemUserSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserResponse) {
            binding.txtUsername.text = user.username

            val baseUrl = NetworkModule.BASE_API_URL.removeSuffix("/")
            val fullUrl = if (user.urlImagen?.startsWith("http") == true) user.urlImagen else baseUrl + (user.urlImagen ?: "")

            Glide.with(binding.ivUserThumb.context)
                .load(fullUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .circleCrop()
                .into(binding.ivUserThumb)

            binding.btnAddFriend.setOnClickListener {
                onAddClick(user)
            }

            if (isSentCheck(user.id ?: 0L)) {
                binding.btnAddFriend.text = "Enviada"
                binding.btnAddFriend.isEnabled = false
                binding.btnAddFriend.setBackgroundColor(android.graphics.Color.GRAY)
            } else {
                binding.btnAddFriend.text = "Añadir"
                binding.btnAddFriend.isEnabled = true
                val typedValue = android.util.TypedValue()
                itemView.context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
                binding.btnAddFriend.setBackgroundColor(typedValue.data)
            }
        }
    }
}
