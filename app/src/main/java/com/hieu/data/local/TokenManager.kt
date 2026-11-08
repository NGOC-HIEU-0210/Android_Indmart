package com.hieu.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hieu.core.util.JwtUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class UserSession(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val email: String? = null,
    val isAdmin: Boolean = false
)

interface TokenManager {
    val sessionState: StateFlow<UserSession>
    fun saveTokens(accessToken: String, refreshToken: String?)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getUsername(): String?
    fun isAdmin(): Boolean
    fun isLoggedIn(): Boolean
    fun clearTokens()
}

@Singleton
class EncryptedTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenManager {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "indmart_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _sessionState = MutableStateFlow(computeSession())
    override val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    private fun computeSession(): UserSession {
        val token = getAccessToken()
        if (token.isNullOrBlank()) {
            return UserSession(isLoggedIn = false)
        }
        val decoded = JwtUtils.decode(token)
        return UserSession(
            isLoggedIn = true,
            username = decoded?.username ?: sharedPreferences.getString(KEY_USERNAME, null),
            email = decoded?.email,
            isAdmin = decoded?.isAdmin ?: sharedPreferences.getBoolean(KEY_IS_ADMIN, false)
        )
    }

    override fun saveTokens(accessToken: String, refreshToken: String?) {
        val decoded = JwtUtils.decode(accessToken)
        val username = decoded?.username
        val isAdmin = decoded?.isAdmin ?: false

        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply {
                if (refreshToken != null) {
                    putString(KEY_REFRESH_TOKEN, refreshToken)
                }
                if (username != null) {
                    putString(KEY_USERNAME, username)
                }
                putBoolean(KEY_IS_ADMIN, isAdmin)
            }
            .apply()

        _sessionState.value = UserSession(
            isLoggedIn = true,
            username = username,
            email = decoded?.email,
            isAdmin = isAdmin
        )
    }

    override fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    override fun isAdmin(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ADMIN, false)
    }

    override fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    override fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USERNAME)
            .remove(KEY_IS_ADMIN)
            .apply()

        _sessionState.value = UserSession(isLoggedIn = false)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_ADMIN = "is_admin"
    }
}

