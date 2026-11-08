package com.hieu.ui.user.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CartDto
import com.hieu.data.remote.dto.CheckoutRequest
import com.hieu.data.remote.dto.OrderDto
import com.hieu.data.repository.CartRepository
import com.hieu.data.repository.OrderRepository
import com.hieu.data.repository.UserRepository
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

data class CheckoutUiState(
    val cartState: UiState<CartDto> = UiState.Loading,
    val customerName: String = "",
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val note: String = "",
    val couponCode: String? = null,
    val paymentMethod: String = "COD", // COD | BANK_TRANSFER
    val nameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val isSubmitting: Boolean = false,
    val generalError: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _orderSuccessEvent = MutableSharedFlow<OrderDto>()
    val orderSuccessEvent: SharedFlow<OrderDto> = _orderSuccessEvent.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // 1. Tải giỏ hàng
        viewModelScope.launch {
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(cartState = UiState.Success(result.data)) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(cartState = UiState.Error(result.message)) }
                }
                is NetworkResult.Loading -> Unit
            }
        }

        // 2. Tự điền thông tin từ hồ sơ người dùng
        viewModelScope.launch {
            when (val result = userRepository.getMyProfile()) {
                is NetworkResult.Success -> {
                    val user = result.data
                    _uiState.update {
                        it.copy(
                            customerName = user.displayName,
                            phone = user.mobile ?: "",
                            address = user.address ?: "",
                            email = user.email ?: ""
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(customerName = name, nameError = null) }
    fun onPhoneChange(phone: String) = _uiState.update { it.copy(phone = phone, phoneError = null) }
    fun onAddressChange(address: String) = _uiState.update { it.copy(address = address, addressError = null) }
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onNoteChange(note: String) = _uiState.update { it.copy(note = note) }
    fun onPaymentMethodChange(method: String) = _uiState.update { it.copy(paymentMethod = method) }

    fun submitOrder() {
        val state = _uiState.value
        val name = state.customerName.trim()
        val phone = state.phone.trim()
        val address = state.address.trim()

        var hasError = false
        var nameErr: String? = null
        var phoneErr: String? = null
        var addrErr: String? = null

        if (name.isBlank()) {
            nameErr = "Vui lòng nhập họ và tên người nhận"
            hasError = true
        }

        if (phone.isBlank()) {
            phoneErr = "Vui lòng nhập số điện thoại nhận hàng"
            hasError = true
        } else if (phone.length < 9) {
            phoneErr = "Số điện thoại không hợp lệ"
            hasError = true
        }

        if (address.isBlank()) {
            addrErr = "Vui lòng nhập địa chỉ giao hàng"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nameError = nameErr,
                    phoneError = phoneErr,
                    addressError = addrErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalError = null) }

            val request = CheckoutRequest(
                customerName = name,
                phone = phone,
                address = address,
                email = state.email.trim().ifBlank { null },
                note = state.note.trim().ifBlank { null },
                couponCode = state.couponCode,
                paymentMethod = state.paymentMethod
            )

            when (val result = orderRepository.checkoutFromCart(request)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _orderSuccessEvent.emit(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            generalError = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}

