package com.hieu.data.repository

import com.hieu.core.util.NetworkResult
import com.hieu.core.util.safeApiCall
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.dto.CartDto
import com.hieu.data.remote.dto.CartItemRequest
import com.hieu.data.remote.dto.CouponApplyRequest
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface CartRepository {
    suspend fun getCart(): NetworkResult<CartDto>
    suspend fun addToCart(productId: Long, quantity: Int = 1): NetworkResult<CartDto>
    suspend fun updateQuantity(productId: Long, quantity: Int): NetworkResult<CartDto>
    suspend fun removeItem(productId: Long): NetworkResult<CartDto>
    suspend fun clearCart(): NetworkResult<Unit>
    suspend fun applyCoupon(code: String, orderTotal: Double): NetworkResult<Double>
}

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val apiService: IndMartApiService,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CartRepository {

    override suspend fun getCart(): NetworkResult<CartDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getCart()
        }
    }

    override suspend fun addToCart(productId: Long, quantity: Int): NetworkResult<CartDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.addToCart(CartItemRequest(productId = productId, quantity = quantity))
        }
    }

    override suspend fun updateQuantity(productId: Long, quantity: Int): NetworkResult<CartDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateCartItemQuantity(productId = productId, quantity = quantity)
        }
    }

    override suspend fun removeItem(productId: Long): NetworkResult<CartDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.removeCartItem(productId = productId)
        }
    }

    override suspend fun clearCart(): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.clearCart()
        }
    }

    override suspend fun applyCoupon(code: String, orderTotal: Double): NetworkResult<Double> {
        return safeApiCall(ioDispatcher, json) {
            apiService.applyCoupon(CouponApplyRequest(code = code.trim(), orderTotal = orderTotal))
        }
    }
}

