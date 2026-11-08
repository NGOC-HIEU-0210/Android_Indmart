package com.hieu.data.repository

import com.hieu.BuildConfig
import com.hieu.core.util.NetworkResult
import com.hieu.data.local.TokenManager
import com.hieu.data.local.UserSession
import com.hieu.data.remote.api.KeycloakApiService
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val keycloakApiService: KeycloakApiService,
    private val tokenManager: TokenManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    override val sessionState: StateFlow<UserSession> = tokenManager.sessionState

    override suspend fun login(username: String, password: String): NetworkResult<UserSession> {
        return withContext(ioDispatcher) {
            try {
                val tokenResponse = keycloakApiService.login(
                    realm = BuildConfig.KEYCLOAK_REALM,
                    grantType = "password",
                    clientId = BuildConfig.KEYCLOAK_CLIENT_ID,
                    username = username.trim(),
                    password = password
                )

                tokenManager.saveTokens(
                    accessToken = tokenResponse.access_token,
                    refreshToken = tokenResponse.refresh_token
                )

                NetworkResult.Success(tokenManager.sessionState.value)
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400, 401 -> "Tên đăng nhập hoặc mật khẩu không chính xác."
                    else -> "Đăng nhập thất bại (Mã lỗi: ${e.code()}). Vui lòng thử lại."
                }
                NetworkResult.Error(message = message, code = e.code())
            } catch (_: IOException) {
                NetworkResult.Error("Không thể kết nối đến máy chủ xác thực Keycloak.")
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Đã có lỗi xảy ra trong quá trình đăng nhập.")
            }
        }
    }

    override suspend fun logout(): NetworkResult<Unit> {
        return withContext(ioDispatcher) {
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                try {
                    keycloakApiService.logout(
                        realm = BuildConfig.KEYCLOAK_REALM,
                        clientId = BuildConfig.KEYCLOAK_CLIENT_ID,
                        refreshToken = refreshToken
                    )
                } catch (_: Exception) {
                    // Tiếp tục xóa token cục bộ ngay cả khi gọi API logout thất bại
                }
            }
            tokenManager.clearTokens()
            NetworkResult.Success(Unit)
        }
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    override fun isAdmin(): Boolean = tokenManager.isAdmin()

    override fun getUsername(): String? = tokenManager.getUsername()
}

