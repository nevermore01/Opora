package com.example.opora

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MaterialAdapter(
    private val context: Context,
    private var materials: List<Material>
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    private val sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var favoritesList: MutableList<Material> = loadFavorites()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        try {
            val view = LayoutInflater.from(context).inflate(R.layout.item_material, parent, false)
            return MaterialViewHolder(view)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Помилка при завантаженні item_material.xml", e)
        }
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val material = materials[position]

        holder.materialName.text = material.name
        holder.materialShortDescription.text = material.shortDescription

        if (material.imageUrls.isNotEmpty()) {
            holder.materialImage.visibility = View.VISIBLE
            Glide.with(context).load(material.imageUrls[0]).into(holder.materialImage)
        } else {
            holder.materialImage.visibility = View.GONE
        }

        if (isFavorite(material)) {
            holder.cardView.strokeColor = Color.parseColor("#A5D6A7") // Light green border
            holder.cardView.strokeWidth = 8
        } else {
            holder.cardView.strokeColor = Color.TRANSPARENT
            holder.cardView.strokeWidth = 0
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MaterialDetailActivity::class.java)
            intent.putExtra("material", material)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return materials.size
    }

    fun updateMaterials(newMaterials: List<Material>) {
        materials = newMaterials
        notifyDataSetChanged()
    }

    fun refreshFavorites() {
        favoritesList = loadFavorites()
        notifyDataSetChanged()
    }

    private fun loadFavorites(): MutableList<Material> {
        val favoritesJson = sharedPreferences.getString("favorites_list", null)
        val type = object : TypeToken<MutableList<Material>>() {}.type
        return if (favoritesJson != null) {
            gson.fromJson(favoritesJson, type)
        } else {
            mutableListOf()
        }
    }

    fun isFavorite(material: Material): Boolean {
        return favoritesList.any { it.id == material.id }
    }

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val materialImage: ImageView = itemView.findViewById(R.id.materialImage)
        val materialName: TextView = itemView.findViewById(R.id.materialName)
        val materialShortDescription: TextView = itemView.findViewById(R.id.materialShortDescription)
        val cardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
    }
}
