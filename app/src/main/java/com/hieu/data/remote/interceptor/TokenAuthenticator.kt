package com.hieu.data.remote.interceptor

import com.hieu.BuildConfig
import com.hieu.data.local.TokenManager
import com.hieu.data.remote.api.KeycloakApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val keycloakApiServiceProvider: Provider<KeycloakApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Tránh vòng lặp vô hạn nếu chính request refresh hoặc login bị 401
        if (response.request.url.encodedPath.contains("openid-connect/token")) {
            return null
        }

        // Nếu request đã thử lại một lần rồi mà vẫn 401 thì dừng, xóa token
        if (responseCount(response) >= 2) {
            tokenManager.clearTokens()
            return null
        }

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

        synchronized(this) {
            val currentToken = tokenManager.getAccessToken()

            // Nếu một luồng khác đã vừa làm mới token thành công, dùng ngay token mới đó
            if (!currentToken.isNullOrBlank() && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                tokenManager.clearTokens()
                return null
            }

            return try {
                // Gọi API Keycloak để cấp lại access token từ refresh token
                val keycloakApi = keycloakApiServiceProvider.get()
                val tokenResponse = runBlocking {
                    keycloakApi.refreshToken(
                        realm = BuildConfig.KEYCLOAK_REALM,
                        clientId = BuildConfig.KEYCLOAK_CLIENT_ID,
                        refreshToken = refreshToken
                    )
                }

                val newAccessToken = tokenResponse.access_token
                val newRefreshToken = tokenResponse.refresh_token ?: refreshToken

                tokenManager.saveTokens(newAccessToken, newRefreshToken)

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } catch (_: Exception) {
                // Refresh thất bại (hết hạn refresh token hoặc bị thu hồi): Xóa token để quay về Login
                tokenManager.clearTokens()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

