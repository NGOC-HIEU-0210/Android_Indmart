package com.hieu.ui.admin.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.BatchDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BatchTab {
    EXPIRING,
    BY_PRODUCT
}

@HiltViewModel
class AdminBatchesViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(BatchTab.EXPIRING)
    val currentTab: StateFlow<BatchTab> = _currentTab.asStateFlow()

    private val _expiringDays = MutableStateFlow(3)
    val expiringDays: StateFlow<Int> = _expiringDays.asStateFlow()

    private val _batchesState = MutableStateFlow<UiState<List<BatchDto>>>(UiState.Loading)
    val batchesState: StateFlow<UiState<List<BatchDto>>> = _batchesState.asStateFlow()

    private val _productIdInput = MutableStateFlow("")
    val productIdInput: StateFlow<String> = _productIdInput.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isWritingOff = MutableStateFlow(false)
    val isWritingOff: StateFlow<Boolean> = _isWritingOff.asStateFlow()

    init {
        loadExpiringBatches(3)
    }

    fun selectTab(tab: BatchTab) {
        _currentTab.value = tab
        if (tab == BatchTab.EXPIRING) {
            loadExpiringBatches(_expiringDays.value)
        } else {
            val pid = _productIdInput.value.toLongOrNull()
            if (pid != null && pid > 0) {
                loadBatchesByProduct(pid)
            } else {
                _batchesState.value = UiState.Empty
            }
        }
    }

    fun onExpiringDaysChanged(days: Int) {
        _expiringDays.value = days
        loadExpiringBatches(days)
    }

    fun loadExpiringBatches(days: Int) {
        viewModelScope.launch {
            _batchesState.value = UiState.Loading
            when (val result = adminRepository.getExpiringBatches(days = days, page = 0, size = 50)) {
                is NetworkResult.Success -> {
                    val list = result.data.content
                    if (list.isEmpty()) {
                        _batchesState.value = UiState.Empty
                    } else {
                        _batchesState.value = UiState.Success(list)
                    }
                }
                is NetworkResult.Error -> {
                    _batchesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onProductIdChanged(input: String) {
        _productIdInput.value = input
    }

    fun searchByProduct() {
        val pid = _productIdInput.value.toLongOrNull()
        if (pid == null || pid <= 0) {
            _toastMessage.value = "Vui lòng nhập ID sản phẩm hợp lệ"
            return
        }
        loadBatchesByProduct(pid)
    }

    private fun loadBatchesByProduct(productId: Long) {
        viewModelScope.launch {
            _batchesState.value = UiState.Loading
            when (val result = adminRepository.getBatchesByProduct(productId = productId, page = 0, size = 50)) {
                is NetworkResult.Success -> {
                    val list = result.data.content
                    if (list.isEmpty()) {
                        _batchesState.value = UiState.Empty
                    } else {
                        _batchesState.value = UiState.Success(list)
                    }
                }
                is NetworkResult.Error -> {
                    _batchesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun writeOffExpiredBatches() {
        viewModelScope.launch {
            _isWritingOff.value = true
            when (val result = adminRepository.writeOffExpiredBatches()) {
                is NetworkResult.Success -> {
                    _isWritingOff.value = false
                    _toastMessage.value = "Đã xuất hủy thành công ${result.data} lô hàng hết hạn"
                    if (_currentTab.value == BatchTab.EXPIRING) {
                        loadExpiringBatches(_expiringDays.value)
                    } else {
                        val pid = _productIdInput.value.toLongOrNull()
                        if (pid != null) loadBatchesByProduct(pid)
                    }
                }
                is NetworkResult.Error -> {
                    _isWritingOff.value = false
                    _toastMessage.value = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearToast() = _toastMessage.update { null }
}

