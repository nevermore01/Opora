package com.example.opora

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class AdminMaterialAdapter(
    private val context: Context,
    private var materials: List<Material>
) : RecyclerView.Adapter<AdminMaterialAdapter.MaterialViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_admin_material, parent, false)
        return MaterialViewHolder(view)
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

        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditMaterialActivity::class.java)
            intent.putExtra("material", material)
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            db.collection("materials").document(material.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Матеріал видалено", Toast.LENGTH_SHORT).show()
                    materials = materials.toMutableList().apply { removeAt(position) }
                    notifyItemRemoved(position)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Помилка при видаленні матеріалу", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return materials.size
    }

    fun updateMaterials(newMaterials: List<Material>) {
        materials = newMaterials
        notifyDataSetChanged()
    }

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val materialImage: ImageView = itemView.findViewById(R.id.materialImage)
        val materialName: TextView = itemView.findViewById(R.id.materialName)
        val materialShortDescription: TextView = itemView.findViewById(R.id.materialShortDescription)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }
}
