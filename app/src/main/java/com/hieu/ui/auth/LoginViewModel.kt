package com.hieu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LoginNavigationEvent>()
    val navigationEvent: SharedFlow<LoginNavigationEvent> = _navigationEvent.asSharedFlow()

    fun onUsernameChange(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _uiState.value
        val username = currentState.username.trim()
        val password = currentState.password

        var hasError = false
        var usernameError: String? = null
        var passwordError: String? = null

        if (username.isBlank()) {
            usernameError = "Vui lòng nhập tên đăng nhập"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "Vui lòng nhập mật khẩu"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    usernameError = usernameError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            when (val result = authRepository.login(username, password)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    val isAdmin = result.data.isAdmin
                    _navigationEvent.emit(LoginNavigationEvent.LoginSuccess(isAdmin))
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}

