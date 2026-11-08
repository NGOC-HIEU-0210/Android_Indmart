package com.hieu.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class TokenResponse(
    val access_token: String,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val refresh_expires_in: Long? = null,
    val scope: String? = null
)

