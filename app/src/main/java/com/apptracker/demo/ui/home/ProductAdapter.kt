package com.apptracker.demo.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apptracker.demo.databinding.ItemProductBinding
import com.apptracker.demo.data.model.Product

class ProductAdapter(
    private val products: List<Product>,
    private val onItemAction: (Product, Action) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    enum class Action {
        VIEW_DETAILS,
        ADD_TO_CART
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.textProductName.text = product.name
            binding.textProductDescription.text = product.description
            binding.textProductPrice.text = "$${String.format("%.2f", product.price)}"

            binding.buttonViewDetails.setOnClickListener {
                onItemAction(product, Action.VIEW_DETAILS)
            }

            binding.buttonAddToCart.setOnClickListener {
                onItemAction(product, Action.ADD_TO_CART)
            }
        }
    }
}
