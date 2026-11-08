package com.hieu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageDto(
    val id: Long? = null,
    val imageUrl: String? = null,
    val productId: Long? = null
)

@Serializable
data class ProductDto(
    val id: Long? = null,
    val name: String = "",
    val image: String? = null,
    val price: Double = 0.0,
    val active: Boolean? = true,
    val description: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val createdDate: String? = null,
    val updatedDate: String? = null,
    val images: List<ProductImageDto> = emptyList(),
    val favouriteCount: Long? = 0,
    val stock: Int? = 0,
    val shelfLifeDays: Int? = null,
    val currentPrice: Double? = null,
    val discountPercent: Int? = 0,
    val nearestExpiry: String? = null,
    val daysLeft: Int? = null
) {
    /**
     * Giá hiển thị thực tế (ưu tiên currentPrice nếu có giảm giá)
     */
    val effectivePrice: Double
        get() = currentPrice ?: price

    /**
     * Có đang giảm giá giải cứu không
     */
    val hasDiscount: Boolean
        get() = (discountPercent ?: 0) > 0
}

@Serializable
data class CategoryDto(
    val id: Long? = null,
    val name: String = "",
    val image: String? = null,
    val active: Boolean? = true,
    val parentCategoryId: Long? = null,
    val parentCategoryName: String? = null,
    val children: List<CategoryDto> = emptyList()
)

