package com.hieu.core.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.hieu.BuildConfig
import com.hieu.core.theme.GreenPrimary

object CustomTabsHelper {

    /**
     * Mở Chrome Custom Tabs với màu thanh công cụ xanh lá thương hiệu IND MART
     */
    fun openUrl(context: Context, url: String) {
        val colorParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(GreenPrimary.value.toInt())
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorParams)
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    /**
     * Đường dẫn trang đăng ký Keycloak
     */
    fun getRegistrationUrl(): String {
        val base = BuildConfig.KEYCLOAK_URL.trimEnd('/')
        val realm = BuildConfig.KEYCLOAK_REALM
        val clientId = BuildConfig.KEYCLOAK_CLIENT_ID
        val redirectUri = Uri.encode(BuildConfig.AUTH_REDIRECT_URI)
        return "$base/realms/$realm/protocol/openid-connect/registrations?client_id=$clientId&response_type=code&scope=openid&redirect_uri=$redirectUri"
    }

    /**
     * Đường dẫn trang đặt lại mật khẩu Keycloak
     */
    fun getResetPasswordUrl(): String {
        val base = BuildConfig.KEYCLOAK_URL.trimEnd('/')
        val realm = BuildConfig.KEYCLOAK_REALM
        val clientId = BuildConfig.KEYCLOAK_CLIENT_ID
        return "$base/realms/$realm/login-actions/reset-credentials?client_id=$clientId"
    }
}
