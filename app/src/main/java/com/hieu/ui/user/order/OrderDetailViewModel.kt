package com.hieu.ui.user.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.OrderDto
import com.hieu.data.remote.dto.OrderTrackingDto
import com.hieu.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val orderState: UiState<OrderDto> = UiState.Loading,
    val trackingList: List<OrderTrackingDto> = emptyList(),
    val isCancelling: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(orderState = UiState.Loading) }

            // 1. Lấy thông tin đơn hàng
            when (val result = orderRepository.getOrderById(orderId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(orderState = UiState.Success(result.data)) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(orderState = UiState.Error(result.message)) }
                }
                is NetworkResult.Loading -> Unit
            }

            // 2. Lấy lịch sử tracking
            when (val trackResult = orderRepository.getOrderTracking(orderId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(trackingList = trackResult.data) }
                }
                else -> Unit
            }
        }
    }

    fun cancelOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            when (val result = orderRepository.cancelOrder(orderId)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            orderState = UiState.Success(result.data),
                            toastMessage = "Đã hủy đơn hàng thành công"
                        )
                    }
                    // Tải lại tracking sau khi hủy
                    val trackResult = orderRepository.getOrderTracking(orderId)
                    if (trackResult is NetworkResult.Success) {
                        _uiState.update { it.copy(trackingList = trackResult.data) }
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            toastMessage = result.message
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

