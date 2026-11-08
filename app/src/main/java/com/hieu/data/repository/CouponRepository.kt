package com.hieu.data.repository

import com.hieu.core.util.NetworkResult
import com.hieu.core.util.safeApiCall
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.dto.CouponDto
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface CouponRepository {
    suspend fun getActiveCoupons(): NetworkResult<List<CouponDto>>
}

@Singleton
class CouponRepositoryImpl @Inject constructor(
    private val apiService: IndMartApiService,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CouponRepository {

    override suspend fun getActiveCoupons(): NetworkResult<List<CouponDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getActiveCoupons()
        }
    }
}

