package com.example.opora

import java.io.Serializable

data class Material(
    val id: String = "",
    val name: String = "",
    val shortDescription: String = "",
    val detailedDescription: String = "",
    val link: String = "",
    val address: String = "",
    val collectionAmount: String = "",
    val imageUrls: MutableList<String> = mutableListOf(),
    val type: String = ""
) : Serializable

