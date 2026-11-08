package com.hieu.ui.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.DashboardDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<UiState<DashboardDto>>(UiState.Loading)
    val dashboardState: StateFlow<UiState<DashboardDto>> = _dashboardState.asStateFlow()

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            when (val result = adminRepository.getDashboardStats()) {
                is NetworkResult.Success -> {
                    _dashboardState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _dashboardState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}

