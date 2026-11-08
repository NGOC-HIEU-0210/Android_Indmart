package com.hieu.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashNavigationEvent {
    data object NavigateToAdmin : SplashNavigationEvent
    data object NavigateToUser : SplashNavigationEvent
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashNavigationEvent>()
    val navigationEvent: SharedFlow<SplashNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Hiển thị màn hình khởi động trong khoảng thời gian ngắn (800ms) để tải trạng thái
            delay(800)
            val session = tokenManager.sessionState.value
            if (session.isLoggedIn && session.isAdmin) {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToAdmin)
            } else {
                _navigationEvent.emit(SplashNavigationEvent.NavigateToUser)
            }
        }
    }
}
