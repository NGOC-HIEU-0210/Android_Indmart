package com.hieu.ui.admin.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CouponDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCouponsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _couponsState = MutableStateFlow<UiState<List<CouponDto>>>(UiState.Loading)
    val couponsState: StateFlow<UiState<List<CouponDto>>> = _couponsState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadCoupons()
    }

    fun loadCoupons() {
        viewModelScope.launch {
            _couponsState.value = UiState.Loading
            when (val result = adminRepository.getCouponsPaged(page = 0, size = 50)) {
                is NetworkResult.Success -> {
                    val list = result.data.content
                    if (list.isEmpty()) {
                        _couponsState.value = UiState.Empty
                    } else {
                        _couponsState.value = UiState.Success(list)
                    }
                }
                is NetworkResult.Error -> {
                    _couponsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteCoupon(id: Long) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteCoupon(id)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Đã xóa mã giảm giá thành công"
                    loadCoupons()
                }
                is NetworkResult.Error -> {
                    _toastMessage.value = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearToast() = _toastMessage.update { null }
}

