package com.apptracker.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apptracker.demo.annotations.*
import com.apptracker.demo.databinding.ActivityProfileBinding
import com.apptracker.demo.tracking.TrackingInterceptor
import com.apptracker.sdk.AppTracker

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupBottomNavigation()
    }

    @TrackScreenView(screenName = "Profile")
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
    }

    private fun setupUI() {
        // Simple profile screen - no SDK configuration visible to user
        binding.textProfileName.text = "Demo User"
        binding.textProfileEmail.text = "user@example.com"
    }

    private fun setupBottomNavigation() {
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
                    // Already on profile
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
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            binding.bottomNavigation.visibility = android.view.View.VISIBLE
            binding.bottomNavigation.bringToFront()
        }
    }
}



