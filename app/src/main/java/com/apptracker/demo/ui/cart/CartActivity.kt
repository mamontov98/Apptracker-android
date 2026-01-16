package com.apptracker.demo.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.apptracker.demo.R
import com.apptracker.demo.annotations.*
import com.apptracker.demo.databinding.ActivityCartBinding
import com.apptracker.demo.data.manager.CartManager
import com.apptracker.demo.tracking.TrackingInterceptor
import com.apptracker.demo.ui.home.HomeActivity
import com.apptracker.demo.ui.profile.ProfileActivity
import com.apptracker.sdk.AppTracker

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBottomNavigation()
        updateTotal()
    }

    @TrackScreenView(screenName = "Cart")
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
        
        val itemCount = CartManager.getItemCount()
        val totalValue = CartManager.getTotalPrice()
        trackViewCart(itemCount, totalValue)
        
        updateTotal()
        cartAdapter.notifyDataSetChanged()
    }
    
    @TrackViewCart
    private fun trackViewCart(itemCount: Int, totalValue: Double) {
        try {
            val method = this::class.java.getDeclaredMethod("trackViewCart", Int::class.java, Double::class.java)
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, itemCount, totalValue)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing trackViewCart tracking: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(CartManager.getItems()) { productId, productName ->
            onRemoveItemClick(productId, productName)
        }

        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }
    
    @TrackRemoveFromCart
    private fun onRemoveItemClick(productId: String, productName: String) {
        try {
            val method = this::class.java.getDeclaredMethod("onRemoveItemClick", String::class.java, String::class.java)
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, productId, productName)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onRemoveItemClick tracking: ${e.message}", e)
        }
        
        CartManager.removeItem(productId)
        cartAdapter.notifyDataSetChanged()
        updateTotal()
        Toast.makeText(this, "$productName removed from cart", Toast.LENGTH_SHORT).show()
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
                    // Already on cart
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

    private fun updateTotal() {
        val total = CartManager.getTotalPrice()
        val itemCount = CartManager.getItemCount()
        
        binding.textTotalPrice.text = "Total: $${String.format("%.2f", total)}"
        binding.textItemCount.text = "$itemCount items"

        binding.buttonCheckout.setOnClickListener {
            if (CartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onCheckoutClick(total, itemCount)
        }
    }
    
    @TrackButtonClick(buttonId = "checkout", buttonText = "Checkout")
    @TrackCheckoutStarted
    private fun onCheckoutClick(total: Double, itemCount: Int) {
        try {
            val method = this::class.java.getDeclaredMethod("onCheckoutClick", Double::class.java, Int::class.java)
            method.isAccessible = true
            TrackingInterceptor.processMethod(this, method, total, itemCount)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error processing onCheckoutClick tracking: ${e.message}", e)
        }
        Toast.makeText(this, "Checkout started!", Toast.LENGTH_SHORT).show()
    }
}
