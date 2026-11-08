package com.hieu.data.repository

import com.hieu.core.util.NetworkResult
import com.hieu.core.util.safeApiCall
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.dto.UserDto
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    suspend fun getMyProfile(): NetworkResult<UserDto>
    suspend fun updateMyProfile(profile: UserDto): NetworkResult<UserDto>
    suspend fun uploadMyAvatar(filePart: MultipartBody.Part): NetworkResult<UserDto>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: IndMartApiService,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override suspend fun getMyProfile(): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getMyProfile()
        }
    }

    override suspend fun updateMyProfile(profile: UserDto): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateMyProfile(profile)
        }
    }

    override suspend fun uploadMyAvatar(filePart: MultipartBody.Part): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.uploadMyAvatar(filePart)
        }
    }
}

