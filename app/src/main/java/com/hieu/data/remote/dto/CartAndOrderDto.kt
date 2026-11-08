package com.hieu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    val productId: Long,
    val productName: String = "",
    val productImage: String? = null,
    val price: Double = 0.0,
    val originalPrice: Double? = null,
    val discountPercent: Int? = 0,
    val quantity: Int = 1,
    val subtotal: Double = 0.0,
    val available: Boolean? = true
)

@Serializable
data class CartDto(
    val items: List<CartItemDto> = emptyList(),
    val totalQuantity: Int = 0,
    val subtotal: Double = 0.0
)

@Serializable
data class OrderItemDto(
    val productId: Long,
    val productName: String = "",
    val productImage: String? = null,
    val quantity: Int = 1,
    val price: Double = 0.0,
    val subtotal: Double = 0.0
)

@Serializable
data class OrderTrackingDto(
    val status: String? = null,
    val note: String? = null,
    val time: String? = null
)

@Serializable
data class OrderStatusDto(
    val id: Long,
    val name: String,
    val description: String? = null
)

@Serializable
data class OrderDto(
    val id: Long? = null,
    val orderCode: String? = null,
    val customerName: String = "",
    val email: String? = null,
    val phone: String = "",
    val address: String = "",
    val note: String? = null,
    val orderDate: String? = null,
    val total: Double = 0.0,
    val status: String? = null,
    val statusId: Long? = null,
    val couponCode: String? = null,
    val userId: Long? = null,
    val items: List<OrderItemDto> = emptyList(),
    val paymentMethod: String? = null,
    val paymentStatus: String? = null
)

