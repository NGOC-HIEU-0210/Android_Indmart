package com.hieu.ui.admin.coupon

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.data.remote.dto.CouponDto
import com.hieu.data.repository.AdminRepository
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

data class CouponFormState(
    val id: Long? = null,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val discountType: String = "PERCENT", // "PERCENT" or "FIXED"
    val discountValue: String = "",
    val minOrderValue: String = "",
    val maxDiscount: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val usageLimit: String = "",
    val active: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminCouponEditViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val couponId: Long? = savedStateHandle.get<Long>("couponId")?.takeIf { it > 0 }

    private val _formState = MutableStateFlow(CouponFormState(id = couponId))
    val formState: StateFlow<CouponFormState> = _formState.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess: SharedFlow<Boolean> = _saveSuccess.asSharedFlow()

    init {
        couponId?.let { loadCoupon(it) }
    }

    private fun loadCoupon(id: Long) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getCouponById(id)) {
                is NetworkResult.Success -> {
                    val coupon = result.data
                    _formState.update {
                        it.copy(
                            id = coupon.id,
                            code = coupon.code,
                            name = coupon.name ?: "",
                            description = coupon.description ?: "",
                            discountType = coupon.discountType,
                            discountValue = if (coupon.discountValue % 1.0 == 0.0) coupon.discountValue.toLong().toString() else coupon.discountValue.toString(),
                            minOrderValue = coupon.minOrderValue?.let { v -> if (v % 1.0 == 0.0) v.toLong().toString() else v.toString() } ?: "",
                            maxDiscount = coupon.maxDiscount?.let { v -> if (v % 1.0 == 0.0) v.toLong().toString() else v.toString() } ?: "",
                            startDate = coupon.startDate ?: "",
                            endDate = coupon.endDate ?: "",
                            usageLimit = coupon.usageLimit?.toString() ?: "",
                            active = coupon.active ?: true,
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onCodeChange(code: String) = _formState.update { it.copy(code = code.uppercase()) }
    fun onNameChange(name: String) = _formState.update { it.copy(name = name) }
    fun onDescriptionChange(desc: String) = _formState.update { it.copy(description = desc) }
    fun onDiscountTypeChange(type: String) = _formState.update { it.copy(discountType = type) }
    fun onDiscountValueChange(value: String) = _formState.update { it.copy(discountValue = value) }
    fun onMinOrderValueChange(value: String) = _formState.update { it.copy(minOrderValue = value) }
    fun onMaxDiscountChange(value: String) = _formState.update { it.copy(maxDiscount = value) }
    fun onStartDateChange(date: String) = _formState.update { it.copy(startDate = date) }
    fun onEndDateChange(date: String) = _formState.update { it.copy(endDate = date) }
    fun onUsageLimitChange(limit: String) = _formState.update { it.copy(usageLimit = limit) }
    fun onActiveChange(active: Boolean) = _formState.update { it.copy(active = active) }

    fun saveCoupon() {
        val state = _formState.value
        if (state.code.isBlank()) {
            _formState.update { it.copy(errorMessage = "Vui lòng nhập mã giảm giá") }
            return
        }
        val discountValue = state.discountValue.toDoubleOrNull()
        if (discountValue == null || discountValue <= 0) {
            _formState.update { it.copy(errorMessage = "Giá trị giảm giá không hợp lệ") }
            return
        }

        val couponDto = CouponDto(
            id = state.id,
            code = state.code.trim(),
            name = state.name.trim(),
            description = state.description.takeIf { it.isNotBlank() },
            discountType = state.discountType,
            discountValue = discountValue,
            minOrderValue = state.minOrderValue.toDoubleOrNull(),
            maxDiscount = state.maxDiscount.toDoubleOrNull(),
            startDate = state.startDate.takeIf { it.isNotBlank() },
            endDate = state.endDate.takeIf { it.isNotBlank() },
            usageLimit = state.usageLimit.toIntOrNull(),
            active = state.active
        )

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (state.id != null) {
                adminRepository.updateCoupon(state.id, couponDto)
            } else {
                adminRepository.createCoupon(couponDto)
            }

            when (result) {
                is NetworkResult.Success -> {
                    _formState.update { it.copy(isLoading = false) }
                    _saveSuccess.emit(true)
                }
                is NetworkResult.Error -> {
                    _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearError() = _formState.update { it.copy(errorMessage = null) }
}

