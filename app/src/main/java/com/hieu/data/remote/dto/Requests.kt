package com.hieu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CartItemRequest(
    val productId: Long,
    val quantity: Int = 1
)

@Serializable
data class CheckoutItemRequest(
    val productId: Long,
    val quantity: Int
)

@Serializable
data class CheckoutRequest(
    val customerName: String,
    val email: String? = null,
    val phone: String,
    val address: String,
    val note: String? = null,
    val couponCode: String? = null,
    val paymentMethod: String = "COD", // COD | BANK_TRANSFER
    val items: List<CheckoutItemRequest>? = null,
    val userId: Long? = null,
    val statusId: Long? = null
)

@Serializable
data class CouponApplyRequest(
    val code: String,
    val orderTotal: Double
)

@Serializable
data class BatchRequest(
    val productId: Long,
    val quantity: Int,
    val costPrice: Double? = null,
    val expiryDate: String? = null
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val mobile: String? = null,
    val address: String? = null
)

