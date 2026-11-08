package com.hieu.ui.user.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.local.TokenManager
import com.hieu.data.remote.dto.CartDto
import com.hieu.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val cartState: UiState<CartDto> = UiState.Loading,
    val couponCode: String = "",
    val discountAmount: Double = 0.0,
    val isApplyingCoupon: Boolean = false,
    val couponError: String? = null,
    val couponSuccessMessage: String? = null,
    val toastMessage: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    init {
        loadCart()
    }

    fun loadCart() {
        if (!tokenManager.isLoggedIn()) {
            _uiState.update { it.copy(cartState = UiState.Empty) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cartState = UiState.Loading) }
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    val cart = result.data
                    if (cart.items.isEmpty()) {
                        _uiState.update { it.copy(cartState = UiState.Empty) }
                    } else {
                        _uiState.update { it.copy(cartState = UiState.Success(cart)) }
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(cartState = UiState.Error(result.message)) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun updateQuantity(productId: Long, currentQty: Int, delta: Int) {
        val newQty = currentQty + delta
        if (newQty < 1) return // Ứng dụng chặn số lượng nhỏ hơn 1

        viewModelScope.launch {
            when (val result = cartRepository.updateQuantity(productId, newQty)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            cartState = if (result.data.items.isEmpty()) UiState.Empty else UiState.Success(result.data),
                            // Reset giảm giá khi giỏ hàng đổi
                            discountAmount = 0.0,
                            couponSuccessMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun removeItem(productId: Long) {
        viewModelScope.launch {
            when (val result = cartRepository.removeItem(productId)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            cartState = if (result.data.items.isEmpty()) UiState.Empty else UiState.Success(result.data),
                            discountAmount = 0.0,
                            couponSuccessMessage = null,
                            toastMessage = "Đã xóa sản phẩm khỏi giỏ"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            when (val result = cartRepository.clearCart()) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            cartState = UiState.Empty,
                            couponCode = "",
                            discountAmount = 0.0,
                            toastMessage = "Đã làm trống giỏ hàng"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onCouponCodeChange(code: String) {
        _uiState.update {
            it.copy(
                couponCode = code,
                couponError = null
            )
        }
    }

    fun applyCoupon() {
        val code = _uiState.value.couponCode.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(couponError = "Vui lòng nhập mã giảm giá") }
            return
        }

        val cart = (_uiState.value.cartState as? UiState.Success)?.data
        val orderTotal = cart?.subtotal ?: 0.0

        if (orderTotal <= 0.0) {
            _uiState.update { it.copy(couponError = "Giỏ hàng không có sản phẩm") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingCoupon = true, couponError = null) }
            when (val result = cartRepository.applyCoupon(code, orderTotal)) {
                is NetworkResult.Success -> {
                    val discount = result.data
                    _uiState.update {
                        it.copy(
                            isApplyingCoupon = false,
                            discountAmount = discount,
                            couponSuccessMessage = "Áp dụng thành công mã $code!"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isApplyingCoupon = false,
                            discountAmount = 0.0,
                            couponError = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}

