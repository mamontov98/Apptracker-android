package com.apptracker.demo.tracking

import android.app.Activity
import com.apptracker.demo.annotations.*
import com.apptracker.sdk.AppTracker
import java.lang.reflect.Method

/**
 * Interceptor that processes tracking annotations and calls AppTracker directly
 */
object TrackingInterceptor {
    
    /**
     * Process all tracking annotations on a method and track events
     */
    fun processMethod(activity: Activity, method: Method, vararg args: Any?) {
        if (!AppTracker.isInitialized()) {
            android.util.Log.w("AppTracker", "SDK not initialized, skipping tracking for method: ${method.name}")
            return
        }
        
        // Process TrackButtonClick annotation
        method.getAnnotation(TrackButtonClick::class.java)?.let { annotation ->
            AppTracker.track("button_click", mapOf(
                "button_id" to annotation.buttonId,
                "button_text" to annotation.buttonText,
                "screen_name" to activity::class.java.simpleName
            ))
            android.util.Log.d("AppTracker", "Tracked button_click: ${annotation.buttonId}")
        }
        
        // Process TrackScreenView annotation
        method.getAnnotation(TrackScreenView::class.java)?.let { annotation ->
            AppTracker.track("screen_view", mapOf(
                "screen_name" to annotation.screenName,
                "screen_class" to activity::class.java.simpleName
            ))
            android.util.Log.d("AppTracker", "Tracked screen_view: ${annotation.screenName}")
        }
        
        // Process TrackViewItem annotation
        method.getAnnotation(TrackViewItem::class.java)?.let { annotation ->
            // Use annotation values if provided, otherwise try to get from method args
            val productId = if (annotation.productId.isNotEmpty()) annotation.productId 
                           else getStringArg(args, 0) ?: ""
            val productName = if (annotation.productName.isNotEmpty()) annotation.productName 
                             else getStringArg(args, 1) ?: ""
            val price = if (annotation.price > 0) annotation.price 
                      else getDoubleArg(args, 2) ?: 0.0
            
            if (productId.isNotEmpty() && productName.isNotEmpty()) {
                AppTracker.track("view_item", mapOf(
                    "item_id" to productId,
                    "item_name" to productName,
                    "item_price" to price
                ))
                android.util.Log.d("AppTracker", "Tracked view_item: $productId")
            }
        }
        
        // Process TrackAddToCart annotation
        method.getAnnotation(TrackAddToCart::class.java)?.let { annotation ->
            // Track button click first
            AppTracker.track("button_click", mapOf(
                "button_id" to "add_to_cart",
                "button_text" to "Add to Cart",
                "screen_name" to activity::class.java.simpleName
            ))
            
            // Use annotation values if provided, otherwise try to get from method args
            val productId = if (annotation.productId.isNotEmpty()) annotation.productId 
                           else getStringArg(args, 0) ?: ""
            val productName = if (annotation.productName.isNotEmpty()) annotation.productName 
                             else getStringArg(args, 1) ?: ""
            val price = if (annotation.productPrice > 0) annotation.productPrice 
                      else getDoubleArg(args, 2) ?: 0.0
            val quantity = if (annotation.quantity > 0) annotation.quantity 
                          else getIntArg(args, 3) ?: 1
            
            if (productId.isNotEmpty() && productName.isNotEmpty()) {
                AppTracker.track("add_to_cart", mapOf(
                    "item_id" to productId,
                    "item_name" to productName,
                    "item_price" to price,
                    "quantity" to quantity
                ))
                android.util.Log.d("AppTracker", "Tracked add_to_cart: $productId")
            }
        }
        
        // Process TrackRemoveFromCart annotation
        method.getAnnotation(TrackRemoveFromCart::class.java)?.let { annotation ->
            val productId = if (annotation.productId.isNotEmpty()) annotation.productId 
                           else getStringArg(args, 0) ?: ""
            val productName = if (annotation.productName.isNotEmpty()) annotation.productName 
                             else getStringArg(args, 1) ?: ""
            
            if (productId.isNotEmpty() && productName.isNotEmpty()) {
                AppTracker.track("remove_from_cart", mapOf(
                    "item_id" to productId,
                    "item_name" to productName
                ))
                android.util.Log.d("AppTracker", "Tracked remove_from_cart: $productId")
            }
        }
        
        // Process TrackCheckoutStarted annotation
        method.getAnnotation(TrackCheckoutStarted::class.java)?.let { annotation ->
            val cartValue = if (annotation.cartValue > 0) annotation.cartValue 
                           else getDoubleArg(args, 0) ?: 0.0
            val itemCount = if (annotation.itemCount > 0) annotation.itemCount 
                          else getIntArg(args, 1) ?: 0
            
            AppTracker.track("checkout_started", mapOf(
                "cart_value" to cartValue,
                "item_count" to itemCount
            ))
            android.util.Log.d("AppTracker", "Tracked checkout_started: cartValue=$cartValue, itemCount=$itemCount")
        }
        
        // Process TrackPurchaseInitiated annotation
        method.getAnnotation(TrackPurchaseInitiated::class.java)?.let { annotation ->
            val productId = if (annotation.productId.isNotEmpty()) annotation.productId 
                           else getStringArg(args, 0) ?: ""
            val productName = if (annotation.productName.isNotEmpty()) annotation.productName 
                             else getStringArg(args, 1) ?: ""
            val price = if (annotation.price > 0) annotation.price 
                      else getDoubleArg(args, 2) ?: 0.0
            
            if (productId.isNotEmpty() && productName.isNotEmpty()) {
                AppTracker.track("purchase_initiated", mapOf(
                    "item_id" to productId,
                    "item_name" to productName,
                    "item_price" to price
                ))
                android.util.Log.d("AppTracker", "Tracked purchase_initiated: $productId")
            }
        }
        
        // Process TrackViewCart annotation
        method.getAnnotation(TrackViewCart::class.java)?.let { annotation ->
            val itemCount = if (annotation.itemCount > 0) annotation.itemCount 
                          else getIntArg(args, 0) ?: 0
            val totalValue = if (annotation.totalValue > 0) annotation.totalValue 
                            else getDoubleArg(args, 1) ?: 0.0
            
            AppTracker.track("view_cart", mapOf(
                "item_count" to itemCount,
                "cart_value" to totalValue
            ))
            android.util.Log.d("AppTracker", "Tracked view_cart: itemCount=$itemCount, totalValue=$totalValue")
        }
    }
    
    /**
     * Helper to get string argument from args array
     */
    private fun getStringArg(args: Array<out Any?>, index: Int): String? {
        return if (index < args.size && args[index] is String) {
            args[index] as String
        } else null
    }
    
    /**
     * Helper to get double argument from args array
     */
    private fun getDoubleArg(args: Array<out Any?>, index: Int): Double? {
        return if (index < args.size) {
            when (val arg = args[index]) {
                is Double -> arg
                is Float -> arg.toDouble()
                is Number -> arg.toDouble()
                else -> null
            }
        } else null
    }
    
    /**
     * Helper to get int argument from args array
     */
    private fun getIntArg(args: Array<out Any?>, index: Int): Int? {
        return if (index < args.size) {
            when (val arg = args[index]) {
                is Int -> arg
                is Number -> arg.toInt()
                else -> null
            }
        } else null
    }
    
    /**
     * Call a method with tracking based on its annotations
     */
    inline fun <reified T : Activity> T.callWithTracking(methodName: String, vararg args: Any?) {
        try {
            val paramTypes = args.map { 
                it?.javaClass ?: Any::class.java 
            }.toTypedArray()
            val method = T::class.java.getDeclaredMethod(methodName, *paramTypes)
            method.isAccessible = true
            processMethod(this, method, *args)
            method.invoke(this, *args)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error calling method with tracking: ${e.message}", e)
        }
    }
}
