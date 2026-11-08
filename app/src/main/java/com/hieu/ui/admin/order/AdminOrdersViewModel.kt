package com.hieu.ui.admin.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hieu.core.util.NetworkResult
import com.hieu.data.remote.dto.OrderDto
import com.hieu.data.remote.dto.OrderStatusDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdminOrdersViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _statuses = MutableStateFlow<List<OrderStatusDto>>(emptyList())
    val statuses: StateFlow<List<OrderStatusDto>> = _statuses.asStateFlow()

    private val _selectedStatusId = MutableStateFlow<Long?>(null)
    val selectedStatusId: StateFlow<Long?> = _selectedStatusId.asStateFlow()

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val ordersStream: Flow<PagingData<OrderDto>> = combine(
        _selectedStatusId,
        _searchKeyword.debounce(300)
    ) { statusId, keyword ->
        Pair(statusId, keyword)
    }.flatMapLatest { (statusId, keyword) ->
        adminRepository.getAdminOrdersStream(statusId, keyword.takeIf { it.isNotBlank() })
    }.cachedIn(viewModelScope)

    init {
        loadStatuses()
    }

    private fun loadStatuses() {
        viewModelScope.launch {
            when (val result = adminRepository.getOrderStatuses()) {
                is NetworkResult.Success -> {
                    _statuses.value = result.data
                }
                is NetworkResult.Error -> {
                    _toastMessage.value = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onStatusSelected(statusId: Long?) {
        _selectedStatusId.value = statusId
    }

    fun onKeywordChanged(keyword: String) {
        _searchKeyword.value = keyword
    }

    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteOrder(orderId)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Đã xóa đơn hàng thành công"
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

