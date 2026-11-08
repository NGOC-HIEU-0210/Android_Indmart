package com.hieu.data.remote.interceptor

import com.hieu.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Nếu request đã có Authorization header (hoặc Keycloak direct requests), không ghi đè
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenManager.getAccessToken()
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}

