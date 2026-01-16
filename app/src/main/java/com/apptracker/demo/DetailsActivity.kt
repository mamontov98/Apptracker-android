package com.apptracker.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apptracker.demo.annotations.*
import com.apptracker.demo.databinding.ActivityDetailsBinding
import com.apptracker.demo.model.Product
import com.apptracker.demo.tracking.TrackingInterceptor
import com.apptracker.sdk.AppTracker

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra("product_id") ?: ""
        val productName = intent.getStringExtra("product_name") ?: ""
        val productDescription = intent.getStringExtra("product_description") ?: ""
        val productPrice = intent.getDoubleExtra("product_price", 0.0)

        product = Product(productId, productName, productDescription, productPrice)

        setupUI()
        setupBottomNavigation()
    }

    @TrackScreenView(screenName = "Product Details")
    override fun onResume() {
        super.onResume()
        
        // Process screen view tracking
        if (AppTracker.isInitialized()) {
            try {
                val method = this::class.java.getMethod("onResume")
                TrackingInterceptor.processMethod(this, method)
            } catch (e: Exception) {
                android.util.Log.e("AppTracker", "Error processing onResume tracking: ${e.message}", e)
            }
        }
        
        // Track view item
        trackViewItem(product.id, product.name, product.price)
    }
    
    @TrackViewItem
    private fun trackViewItem(productId: String, productName: String, price: Double) {
        try {
            val method = this::class.java.getDeclaredMethod("trackViewItem", String::class.java, String::class.java, Double::class.java)
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, productId, productName, price)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing trackViewItem tracking: ${e.message}", e)
        }
    }

    private fun setupUI() {
        binding.textProductName.text = product.name
        binding.textProductDescription.text = product.description
        binding.textProductPrice.text = "$${String.format("%.2f", product.price)}"

        binding.buttonAddToCart.setOnClickListener {
            onAddToCartClick()
        }

        binding.buttonBuyNow.setOnClickListener {
            onBuyNowClick()
        }
    }
    
    @TrackAddToCart
    private fun onAddToCartClick() {
        try {
            val method = this::class.java.getDeclaredMethod("onAddToCartClick")
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, product.id, product.name, product.price)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onAddToCartClick tracking: ${e.message}", e)
        }
        
        CartManager.addItem(product)
        Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
    }
    
    @TrackButtonClick(buttonId = "buy_now", buttonText = "Buy Now")
    @TrackPurchaseInitiated
    private fun onBuyNowClick() {
        try {
            val method = this::class.java.getDeclaredMethod("onBuyNowClick")
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, product.id, product.name, product.price)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onBuyNowClick tracking: ${e.message}", e)
        }
        
        Toast.makeText(this, "Purchase initiated for ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        // Ensure bottom navigation is always visible
        binding.bottomNavigation.visibility = android.view.View.VISIBLE
        binding.bottomNavigation.bringToFront()
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    onNavHomeClick()
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
    
    @TrackButtonClick(buttonId = "nav_home", buttonText = "Home")
    private fun onNavHomeClick() {
        try {
            val method = this::class.java.getDeclaredMethod("onNavHomeClick")
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onNavHomeClick tracking: ${e.message}", e)
        }
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
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

