package com.hieu.data.repository

import com.hieu.core.util.NetworkResult
import com.hieu.core.util.safeApiCall
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.dto.FavouriteDto
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface FavouriteRepository {
    suspend fun getFavourites(): NetworkResult<List<FavouriteDto>>
    suspend fun toggleFavourite(productId: Long): NetworkResult<Boolean>
    suspend fun checkFavourite(productId: Long): NetworkResult<Boolean>
    suspend fun getFavouriteCount(productId: Long): NetworkResult<Long>
    suspend fun removeFavourite(productId: Long): NetworkResult<Unit>
}

@Singleton
class FavouriteRepositoryImpl @Inject constructor(
    private val apiService: IndMartApiService,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FavouriteRepository {

    override suspend fun getFavourites(): NetworkResult<List<FavouriteDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getFavourites()
        }
    }

    override suspend fun toggleFavourite(productId: Long): NetworkResult<Boolean> {
        return safeApiCall(ioDispatcher, json) {
            apiService.toggleFavourite(productId)
        }
    }

    override suspend fun checkFavourite(productId: Long): NetworkResult<Boolean> {
        return safeApiCall(ioDispatcher, json) {
            apiService.checkFavourite(productId)
        }
    }

    override suspend fun getFavouriteCount(productId: Long): NetworkResult<Long> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getFavouriteCount(productId)
        }
    }

    override suspend fun removeFavourite(productId: Long): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.removeFavourite(productId)
        }
    }
}

