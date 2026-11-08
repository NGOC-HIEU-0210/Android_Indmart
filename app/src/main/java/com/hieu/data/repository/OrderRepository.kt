package com.hieu.data.repository

import com.hieu.core.util.NetworkResult
import com.hieu.core.util.safeApiCall
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.dto.CheckoutRequest
import com.hieu.data.remote.dto.OrderDto
import com.hieu.data.remote.dto.OrderTrackingDto
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface OrderRepository {
    suspend fun checkoutFromCart(request: CheckoutRequest): NetworkResult<OrderDto>
    suspend fun getMyOrders(): NetworkResult<List<OrderDto>>
    suspend fun getOrderById(id: Long): NetworkResult<OrderDto>
    suspend fun getOrderByCode(orderCode: String): NetworkResult<OrderDto>
    suspend fun getOrderTracking(id: Long): NetworkResult<List<OrderTrackingDto>>
    suspend fun cancelOrder(id: Long): NetworkResult<OrderDto>
}

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: IndMartApiService,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : OrderRepository {

    override suspend fun checkoutFromCart(request: CheckoutRequest): NetworkResult<OrderDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.checkoutFromCart(request)
        }
    }

    override suspend fun getMyOrders(): NetworkResult<List<OrderDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getMyOrders()
        }
    }

    override suspend fun getOrderById(id: Long): NetworkResult<OrderDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getOrderById(id)
        }
    }

    override suspend fun getOrderByCode(orderCode: String): NetworkResult<OrderDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getOrderByCode(orderCode)
        }
    }

    override suspend fun getOrderTracking(id: Long): NetworkResult<List<OrderTrackingDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getOrderTracking(id)
        }
    }

    override suspend fun cancelOrder(id: Long): NetworkResult<OrderDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.cancelOrder(id)
        }
    }
}

