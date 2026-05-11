package com.example.appmusica.presentation.social

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.databinding.ItemFriendBinding
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.di.NetworkModule

class FriendAdapter(private val onClick: (UserResponse) -> Unit) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {
    private var friends: List<UserResponse> = emptyList()

    fun update(newList: List<UserResponse>) {
        friends = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(friends[position])
    override fun getItemCount() = friends.size

    inner class ViewHolder(private val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserResponse) {
            binding.txtUsername.text = user.username
            
            val baseUrl = NetworkModule.BASE_API_URL.removeSuffix("/")
            val fullUrl = if (user.urlImagen?.startsWith("http") == true) user.urlImagen else baseUrl + (user.urlImagen ?: "")
            
            Glide.with(binding.ivFriendThumb.context)
                .load(fullUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .circleCrop()
                .into(binding.ivFriendThumb)

            itemView.setOnClickListener { onClick(user) }
        }
    }
}
