package com.hieu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CouponDto(
    val id: Long? = null,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val discountType: String = "PERCENT", // PERCENT | FIXED
    val discountValue: Double = 0.0,
    val minOrderValue: Double? = 0.0,
    val maxDiscount: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val usageLimit: Int? = null,
    val usedCount: Int? = 0,
    val active: Boolean? = true
)

@Serializable
data class UserDto(
    val id: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val mobile: String? = null,
    val address: String? = null,
    val avatar: String? = null,
    val status: Boolean? = null,
    val createdDate: String? = null,
    val updatedDate: String? = null,
    val roles: List<String> = emptyList()
) {
    val displayName: String
        get() {
            val full = listOfNotNull(lastName, firstName).filter { it.isNotBlank() }.joinToString(" ")
            return full.ifBlank { username ?: "Người dùng" }
        }
}

@Serializable
data class FavouriteDto(
    val id: Long? = null,
    val userId: Long? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val productImage: String? = null,
    val productPrice: Double? = null,
    val createdDate: String? = null
)

@Serializable
data class BatchDto(
    val id: Long? = null,
    val productId: Long,
    val productName: String? = null,
    val initialQuantity: Int? = 0,
    val remainingQuantity: Int? = 0,
    val wastedQuantity: Int? = 0,
    val costPrice: Double? = 0.0,
    val receivedDate: String? = null,
    val expiryDate: String? = null,
    val daysLeft: Int? = null,
    val status: String? = null // ACTIVE | SOLD_OUT | EXPIRED
)

@Serializable
data class WasteReportItemDto(
    val productId: Long,
    val productName: String = "",
    val wastedQuantity: Long = 0,
    val wastedCost: Double = 0.0
)

@Serializable
data class WasteReportDto(
    val from: String? = null,
    val to: String? = null,
    val totalWastedQuantity: Long = 0,
    val totalWastedCost: Double = 0.0,
    val items: List<WasteReportItemDto> = emptyList()
)

@Serializable
data class DashboardDto(
    val totalUsers: Long = 0,
    val totalProducts: Long = 0,
    val totalCategories: Long = 0,
    val totalOrders: Long = 0,
    val totalCoupons: Long = 0,
    val totalRevenue: Double = 0.0,
    val pendingOrders: Long = 0,
    val completedOrders: Long = 0,
    val cancelledOrders: Long = 0
)

