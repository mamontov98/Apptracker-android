package com.apptracker.demo

import com.apptracker.demo.model.Product

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun addItem(product: Product, quantity: Int = 1) {
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cartItems.add(CartItem(product, quantity))
        }
    }

    fun removeItem(productId: String) {
        cartItems.removeAll { it.product.id == productId }
    }

    fun getItems(): List<CartItem> = cartItems.toList()

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    fun getItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }

    fun clear() {
        cartItems.clear()
    }

    fun isEmpty(): Boolean = cartItems.isEmpty()
}



