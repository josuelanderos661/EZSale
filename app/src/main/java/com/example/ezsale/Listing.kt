package com.example.ezsale

data class Listing(
    var id: String = "",
    val title: String = "",
    val price: String = "",
    val category: String = "",
    val condition: String = "",
    val description: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val imagePath: String = ""
)