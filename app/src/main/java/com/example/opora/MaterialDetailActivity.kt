package com.example.opora

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MaterialDetailActivity : AppCompatActivity() {

    private lateinit var materialImageViewPager: ViewPager2
    private lateinit var materialName: TextView
    private lateinit var materialShortDescription: TextView
    private lateinit var materialDetailedDescription: TextView
    private lateinit var materialAddress: TextView
    private lateinit var openLinkButton: Button
    private lateinit var openMapButton: Button
    private lateinit var favoriteButton: Button
    private lateinit var imageUrls: List<String>
    private lateinit var materialLink: String
    private lateinit var materialAddressText: String

    private val sharedPreferences by lazy {
        getSharedPreferences("favorites", Context.MODE_PRIVATE)
    }
    private val gson by lazy { Gson() }
    private val type by lazy { object : TypeToken<MutableList<Material>>() {}.type }
    private val favoritesList by lazy {
        val favoritesJson = sharedPreferences.getString("favorites_list", null)
        if (favoritesJson != null) {
            gson.fromJson<MutableList<Material>>(favoritesJson, type)
        } else {
            mutableListOf()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_detail)

        materialImageViewPager = findViewById(R.id.materialImageViewPager)
        materialName = findViewById(R.id.materialName)
        materialShortDescription = findViewById(R.id.materialShortDescription)
        materialDetailedDescription = findViewById(R.id.materialDetailedDescription)
        materialAddress = findViewById(R.id.materialAddress)
        openLinkButton = findViewById(R.id.openLinkButton)
        openMapButton = findViewById(R.id.openMapButton)
        favoriteButton = findViewById(R.id.favoriteButton)

        val material = intent.getSerializableExtra("material") as Material

        materialName.text = material.name
        materialShortDescription.text = material.shortDescription
        materialDetailedDescription.text = material.detailedDescription
        materialLink = material.link
        materialAddressText = material.address

        if (materialLink.isNotEmpty()) {
            openLinkButton.visibility = View.VISIBLE
            openLinkButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(materialLink))
                startActivity(intent)
            }
        }

        if (materialAddressText.isNotEmpty()) {
            materialAddress.text = materialAddressText
            materialAddress.visibility = View.VISIBLE
            openMapButton.visibility = View.VISIBLE
            openMapButton.setOnClickListener {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(materialAddressText)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        }

        imageUrls = material.imageUrls

        if (imageUrls.isNotEmpty()) {
            materialImageViewPager.visibility = View.VISIBLE
            materialImageViewPager.adapter = ImagePagerAdapter(this, imageUrls)
        }

        setupFavoriteButton(material)
    }

    private fun setupFavoriteButton(material: Material) {
        if (favoritesList.any { it.id == material.id }) {
            favoriteButton.text = "Видалити з обраного"
            favoriteButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        } else {
            favoriteButton.text = "Додати в обране"
            favoriteButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        }

        favoriteButton.setOnClickListener {
            if (favoritesList.any { it.id == material.id }) {
                removeFromFavorites(material)
            } else {
                addToFavorites(material)
            }
        }
    }

    private fun addToFavorites(material: Material) {
        favoritesList.add(material)
        saveFavorites()
        setupFavoriteButton(material)
    }

    private fun removeFromFavorites(material: Material) {
        favoritesList.removeAll { it.id == material.id }
        saveFavorites()
        setupFavoriteButton(material)
    }

    private fun saveFavorites() {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(favoritesList)
        editor.putString("favorites_list", json)
        editor.apply()
    }
}
