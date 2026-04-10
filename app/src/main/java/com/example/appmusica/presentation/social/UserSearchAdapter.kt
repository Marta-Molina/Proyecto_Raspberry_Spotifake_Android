package com.example.appmusica.presentation.social

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appmusica.databinding.ItemUserSearchBinding
import com.example.appmusica.data.remote.response.UserResponse

class UserSearchAdapter(private val onAddClick: (UserResponse) -> Unit) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {
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
            binding.btnAddFriend.setOnClickListener { onAddClick(user) }
        }
    }
}
