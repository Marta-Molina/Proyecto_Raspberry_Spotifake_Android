package com.example.appmusica.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appmusica.databinding.ItemMascotaBinding
import com.example.appmusica.domain.model.Mascota

class MascotaAdapter(
    private var mascotas: List<Mascota> = emptyList(),
    private val onMascotaClick: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.ViewHolder>() {

    fun update(newList: List<Mascota>) {
        mascotas = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMascotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mascotas[position])
    }

    override fun getItemCount() = mascotas.size

    inner class ViewHolder(private val binding: ItemMascotaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mascota: Mascota) {
            binding.txtMascotaName.text = mascota.nombre
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
            val fullUrl = if (mascota.urlSprite.startsWith("http")) mascota.urlSprite else baseUrl + mascota.urlSprite
            
            Glide.with(binding.root).load(fullUrl).into(binding.imgMascota)
            
            binding.root.setOnClickListener { onMascotaClick(mascota) }
            
            // Highlight if active
            binding.imgActiveMark.visibility = if (mascota.esActiva) View.VISIBLE else View.GONE
            
            // Show lock if not premiumDefault (assuming non-premium users can't use them)
            // or if we have more complex logic for esComprada later
            binding.imgLockMascota.visibility = if (!mascota.premiumDefault) View.VISIBLE else View.GONE
        }
    }
}
