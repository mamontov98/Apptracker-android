package com.apptracker.demo.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageResId: Int = 0 // For demo, we'll use placeholder
)



