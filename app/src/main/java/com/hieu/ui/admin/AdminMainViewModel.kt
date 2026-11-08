package com.hieu.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.data.local.UserSession
import com.hieu.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminMainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val sessionState: StateFlow<UserSession> = authRepository.sessionState

    private val _logoutSuccess = MutableSharedFlow<Boolean>()
    val logoutSuccess: SharedFlow<Boolean> = _logoutSuccess.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutSuccess.emit(true)
        }
    }
}

