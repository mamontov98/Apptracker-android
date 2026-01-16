package com.apptracker.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apptracker.demo.databinding.ItemCartBinding

class CartAdapter(
    private val cartItems: List<CartItem>,
    private val onRemoveClick: (String, String) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.textItemName.text = cartItem.product.name
            binding.textItemPrice.text = "$${String.format("%.2f", cartItem.product.price)}"
            binding.textItemQuantity.text = "Qty: ${cartItem.quantity}"
            binding.textItemTotal.text = "Total: $${String.format("%.2f", cartItem.product.price * cartItem.quantity)}"

            binding.buttonRemove.setOnClickListener {
                onRemoveClick(cartItem.product.id, cartItem.product.name)
            }
        }
    }
}



