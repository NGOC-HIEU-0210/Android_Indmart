package com.hieu.ui.auth

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed interface LoginNavigationEvent {
    data class LoginSuccess(val isAdmin: Boolean) : LoginNavigationEvent
}

