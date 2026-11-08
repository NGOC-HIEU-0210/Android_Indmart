package com.hieu.data.repository

import com.hieu.core.util.NetworkResult
import com.hieu.data.local.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionState: StateFlow<UserSession>
    suspend fun login(username: String, password: String): NetworkResult<UserSession>
    suspend fun logout(): NetworkResult<Unit>
    fun isLoggedIn(): Boolean
    fun isAdmin(): Boolean
    fun getUsername(): String?
}

