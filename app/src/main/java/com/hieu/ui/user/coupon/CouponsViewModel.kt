package com.hieu.ui.user.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CouponDto
import com.hieu.data.repository.CouponRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponsViewModel @Inject constructor(
    private val couponRepository: CouponRepository
) : ViewModel() {

    private val _couponsState = MutableStateFlow<UiState<List<CouponDto>>>(UiState.Loading)
    val couponsState: StateFlow<UiState<List<CouponDto>>> = _couponsState.asStateFlow()

    init {
        loadActiveCoupons()
    }

    fun loadActiveCoupons() {
        viewModelScope.launch {
            _couponsState.value = UiState.Loading
            when (val result = couponRepository.getActiveCoupons()) {
                is NetworkResult.Success -> {
                    if (result.data.isEmpty()) {
                        _couponsState.value = UiState.Empty
                    } else {
                        _couponsState.value = UiState.Success(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    _couponsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}

