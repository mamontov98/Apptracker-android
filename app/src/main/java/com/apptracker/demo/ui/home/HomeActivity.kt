package com.apptracker.demo.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.apptracker.demo.R
import com.apptracker.demo.annotations.*
import com.apptracker.demo.databinding.ActivityHomeBinding
import com.apptracker.demo.data.model.Product
import com.apptracker.demo.data.manager.CartManager
import com.apptracker.demo.tracking.TrackingInterceptor
import com.apptracker.demo.ui.cart.CartActivity
import com.apptracker.demo.ui.profile.ProfileActivity
import com.apptracker.sdk.AppTracker

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var productAdapter: ProductAdapter

    private val products = listOf(
        Product("1", "Laptop", "High-performance laptop for work and gaming", 1299.99),
        Product("2", "Smartphone", "Latest smartphone with amazing camera", 899.99),
        Product("3", "Headphones", "Wireless noise-cancelling headphones", 299.99),
        Product("4", "Smartwatch", "Fitness tracking smartwatch", 399.99),
        Product("5", "Tablet", "10-inch tablet for entertainment", 599.99),
        Product("6", "Keyboard", "Mechanical gaming keyboard", 149.99)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBottomNavigation()
    }

    @TrackScreenView(screenName = "Home")
    override fun onResume() {
        super.onResume()
        
        // Process tracking annotation
        if (AppTracker.isInitialized()) {
            try {
                val method = this::class.java.getMethod("onResume")
                TrackingInterceptor.processMethod(this, method)
            } catch (e: Exception) {
                android.util.Log.e("AppTracker", "Error processing onResume tracking: ${e.message}", e)
            }
        } else {
            android.util.Log.w("AppTracker", "SDK not initialized yet, will retry in 1 second")
            // Retry after 1 second in case SDK is still initializing
            binding.root.postDelayed({
                if (AppTracker.isInitialized()) {
                    try {
                        val method = this::class.java.getMethod("onResume")
                        TrackingInterceptor.processMethod(this, method)
                    } catch (e: Exception) {
                        android.util.Log.e("AppTracker", "Error processing onResume tracking: ${e.message}", e)
                    }
                } else {
                    android.util.Log.e("AppTracker", "SDK still not initialized after delay")
                }
            }, 1000)
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products) { product, action ->
            when (action) {
                ProductAdapter.Action.VIEW_DETAILS -> {
                    onViewDetailsClick(product)
                }
                ProductAdapter.Action.ADD_TO_CART -> {
                    onAddToCartClick(product)
                }
            }
        }

        binding.recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = productAdapter
        }
    }
    
    @TrackButtonClick(buttonId = "view_details", buttonText = "View Details")
    @TrackViewItem
    private fun onViewDetailsClick(product: Product) {
        // Process tracking annotations
        try {
            val method = this::class.java.getDeclaredMethod("onViewDetailsClick", Product::class.java)
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, product.id, product.name, product.price)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onViewDetailsClick tracking: ${e.message}", e)
        }
        
        val intent = Intent(this, com.apptracker.demo.ui.details.DetailsActivity::class.java).apply {
            putExtra("product_id", product.id)
            putExtra("product_name", product.name)
            putExtra("product_description", product.description)
            putExtra("product_price", product.price)
        }
        startActivity(intent)
    }
    
    @TrackAddToCart
    private fun onAddToCartClick(product: Product) {
        // Process tracking annotation
        try {
            val method = this::class.java.getDeclaredMethod("onAddToCartClick", Product::class.java)
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, product.id, product.name, product.price)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onAddToCartClick tracking: ${e.message}", e)
        }
        
        CartManager.addItem(product)
        Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        // Ensure bottom navigation is always visible
        binding.bottomNavigation.visibility = android.view.View.VISIBLE
        binding.bottomNavigation.bringToFront()
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_cart -> {
                    onNavCartClick()
                    true
                }
                R.id.nav_profile -> {
                    onNavProfileClick()
                    true
                }
                else -> false
            }
        }
    }
    
    @TrackButtonClick(buttonId = "nav_cart", buttonText = "Cart")
    private fun onNavCartClick() {
        try {
            val method = this::class.java.getDeclaredMethod("onNavCartClick")
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onNavCartClick tracking: ${e.message}", e)
        }
        startActivity(Intent(this, CartActivity::class.java))
    }
    
    @TrackButtonClick(buttonId = "nav_profile", buttonText = "Profile")
    private fun onNavProfileClick() {
        try {
            val method = this::class.java.getDeclaredMethod("onNavProfileClick")
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onNavProfileClick tracking: ${e.message}", e)
        }
        startActivity(Intent(this, ProfileActivity::class.java))
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Ensure bottom navigation is visible when window gains focus
            binding.bottomNavigation.visibility = android.view.View.VISIBLE
            binding.bottomNavigation.bringToFront()
        }
    }
}
