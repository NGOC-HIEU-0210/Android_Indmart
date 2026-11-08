package com.hieu.ui.user.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.OrderDto
import com.hieu.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ordersState = MutableStateFlow<UiState<List<OrderDto>>>(UiState.Loading)
    val ordersState: StateFlow<UiState<List<OrderDto>>> = _ordersState.asStateFlow()

    init {
        loadMyOrders()
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _ordersState.value = UiState.Loading
            when (val result = orderRepository.getMyOrders()) {
                is NetworkResult.Success -> {
                    if (result.data.isEmpty()) {
                        _ordersState.value = UiState.Empty
                    } else {
                        _ordersState.value = UiState.Success(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    _ordersState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}

