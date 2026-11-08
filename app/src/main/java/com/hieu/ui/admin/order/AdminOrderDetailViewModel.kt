package com.hieu.ui.admin.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.OrderDto
import com.hieu.data.remote.dto.OrderStatusDto
import com.hieu.data.remote.dto.OrderTrackingDto
import com.hieu.data.repository.AdminRepository
import com.hieu.data.repository.OrderRepository
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

data class AdminOrderDetailState(
    val order: OrderDto? = null,
    val tracking: List<OrderTrackingDto> = emptyList(),
    val statuses: List<OrderStatusDto> = emptyList(),
    val isUpdating: Boolean = false
)

@HiltViewModel
class AdminOrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L

    private val _uiState = MutableStateFlow<UiState<AdminOrderDetailState>>(UiState.Loading)
    val uiState: StateFlow<UiState<AdminOrderDetailState>> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _orderDeleted = MutableSharedFlow<Boolean>()
    val orderDeleted: SharedFlow<Boolean> = _orderDeleted.asSharedFlow()

    init {
        loadOrderDetail()
    }

    fun loadOrderDetail() {
        if (orderId <= 0L) {
            _uiState.value = UiState.Error("ID đơn hàng không hợp lệ")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val orderResult = orderRepository.getOrderById(orderId)
            val trackingResult = orderRepository.getOrderTracking(orderId)
            val statusesResult = adminRepository.getOrderStatuses()

            if (orderResult is NetworkResult.Success) {
                val trackingList = if (trackingResult is NetworkResult.Success) trackingResult.data else emptyList()
                val statusesList = if (statusesResult is NetworkResult.Success) statusesResult.data else emptyList()
                _uiState.value = UiState.Success(
                    AdminOrderDetailState(
                        order = orderResult.data,
                        tracking = trackingList,
                        statuses = statusesList
                    )
                )
            } else if (orderResult is NetworkResult.Error) {
                _uiState.value = UiState.Error(orderResult.message)
            }
        }
    }

    fun updateStatus(statusId: Long) {
        viewModelScope.launch {
            val currentState = (_uiState.value as? UiState.Success)?.data ?: return@launch
            _uiState.value = UiState.Success(currentState.copy(isUpdating = true))

            when (val result = adminRepository.updateOrderStatus(orderId, statusId)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Cập nhật trạng thái thành công"
                    loadOrderDetail()
                }
                is NetworkResult.Error -> {
                    _toastMessage.value = result.message
                    _uiState.value = UiState.Success(currentState.copy(isUpdating = false))
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun updatePaymentStatus(paymentStatus: String) {
        viewModelScope.launch {
            val currentState = (_uiState.value as? UiState.Success)?.data ?: return@launch
            _uiState.value = UiState.Success(currentState.copy(isUpdating = true))

            when (val result = adminRepository.updateOrderPaymentStatus(orderId, paymentStatus)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Cập nhật trạng thái thanh toán thành công"
                    loadOrderDetail()
                }
                is NetworkResult.Error -> {
                    _toastMessage.value = result.message
                    _uiState.value = UiState.Success(currentState.copy(isUpdating = false))
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteOrder() {
        viewModelScope.launch {
            val currentState = (_uiState.value as? UiState.Success)?.data ?: return@launch
            _uiState.value = UiState.Success(currentState.copy(isUpdating = true))

            when (val result = adminRepository.deleteOrder(orderId)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Đã xóa đơn hàng thành công"
                    _orderDeleted.emit(true)
                }
                is NetworkResult.Error -> {
                    _toastMessage.value = result.message
                    _uiState.value = UiState.Success(currentState.copy(isUpdating = false))
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearToast() = _toastMessage.update { null }
}

