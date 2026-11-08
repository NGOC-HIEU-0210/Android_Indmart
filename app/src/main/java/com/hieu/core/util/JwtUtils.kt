package com.hieu.core.util

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

data class DecodedJwt(
    val username: String?,
    val email: String?,
    val fullName: String?,
    val roles: List<String>,
    val isAdmin: Boolean,
    val expiresAt: Long
)

object JwtUtils {

    /**
     * Giải mã payload của JWT không cần thư viện bên ngoài.
     * Trả về DecodedJwt chứa preferred_username, email, roles và cờ isAdmin.
     */
    fun decode(token: String?): DecodedJwt? {
        if (token.isNullOrBlank()) return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payloadEncoded = parts[1]
            val decodedBytes = Base64.decode(payloadEncoded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val jsonString = String(decodedBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonString)

            val username = if (json.has("preferred_username")) json.optString("preferred_username") else null
            val email = if (json.has("email")) json.optString("email") else null
            val name = if (json.has("name")) json.optString("name") else null
            val exp = json.optLong("exp", 0L)

            val roles = mutableListOf<String>()
            val realmAccess = json.optJSONObject("realm_access")
            if (realmAccess != null) {
                val rolesArray = realmAccess.optJSONArray("roles")
                if (rolesArray != null) {
                    for (i in 0 until rolesArray.length()) {
                        roles.add(rolesArray.getString(i))
                    }
                }
            }

            val isAdmin = roles.any { it.equals("ADMIN", ignoreCase = true) }

            DecodedJwt(
                username = username,
                email = email,
                fullName = name,
                roles = roles,
                isAdmin = isAdmin,
                expiresAt = exp
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Kiểm tra token đã hết hạn chưa (dự phòng thêm 60 giây buffer)
     */
    fun isExpired(token: String?): Boolean {
        val decoded = decode(token) ?: return true
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        return decoded.expiresAt <= (currentTimeSeconds + 60)
    }
}

