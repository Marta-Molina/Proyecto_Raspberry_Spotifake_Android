package com.example.appmusica.presentation.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.R
import com.example.appmusica.data.remote.response.UserResponse
import com.example.appmusica.di.NetworkModule

class UserAdapter(
    private var users: List<UserResponse>,
    private val onPromote: (UserResponse) -> Unit,
    private val onDelete: (UserResponse) -> Unit,
    private val onClick: (UserResponse) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivUserPhoto: ImageView = view.findViewById(R.id.ivUserPhoto)
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvAdminBadge: TextView = view.findViewById(R.id.tvAdminBadge)
        val btnPromoteAdmin: ImageButton = view.findViewById(R.id.btnPromoteAdmin)
        val btnDeleteUser: ImageButton = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUserName.text = user.nombre
        holder.tvUserEmail.text = user.correo
        holder.tvAdminBadge.visibility = if (user.admin) View.VISIBLE else View.GONE
        
        // Hide promote button if already admin
        holder.btnPromoteAdmin.visibility = if (user.admin) View.GONE else View.VISIBLE

        val baseUrl = NetworkModule.BASE_URL.replace("/api/", "")
        user.urlImagen?.let { url ->
            Glide.with(holder.itemView.context)
                .load(baseUrl + url)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .circleCrop()
                .into(holder.ivUserPhoto)
        } ?: run {
            holder.ivUserPhoto.setImageResource(R.drawable.user)
        }

        holder.btnPromoteAdmin.setOnClickListener { onPromote(user) }
        holder.btnDeleteUser.setOnClickListener { onDelete(user) }
        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<UserResponse>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
