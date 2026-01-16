package com.apptracker.demo.annotations

/**
 * Annotation for tracking button clicks
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackButtonClick(
    val buttonId: String,
    val buttonText: String
)

/**
 * Annotation for tracking screen views
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackScreenView(
    val screenName: String
)

/**
 * Annotation for tracking view item events
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackViewItem(
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0
)

/**
 * Annotation for tracking add to cart events
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackAddToCart(
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 1
)

/**
 * Annotation for tracking remove from cart events
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackRemoveFromCart(
    val productId: String = "",
    val productName: String = ""
)

/**
 * Annotation for tracking checkout started events
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackCheckoutStarted(
    val cartValue: Double = 0.0,
    val itemCount: Int = 0
)

/**
 * Annotation for tracking purchase initiated events
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackPurchaseInitiated(
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0
)

/**
 * Annotation for tracking view cart events
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TrackViewCart(
    val itemCount: Int = 0,
    val totalValue: Double = 0.0
)
