package com.hieu.ui.admin.batch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.data.remote.dto.BatchRequest
import com.hieu.data.remote.dto.ProductDto
import com.hieu.data.repository.AdminRepository
import com.hieu.data.repository.ProductRepository
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

data class BatchReceiveFormState(
    val productId: String = "",
    val productName: String? = null,
    val quantity: String = "1",
    val costPrice: String = "",
    val expiryDate: String = "",
    val isLoading: Boolean = false,
    val isFetchingProduct: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminBatchReceiveViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialProductId: Long? = savedStateHandle.get<Long>("productId")?.takeIf { it > 0 }

    private val _formState = MutableStateFlow(
        BatchReceiveFormState(productId = initialProductId?.toString() ?: "")
    )
    val formState: StateFlow<BatchReceiveFormState> = _formState.asStateFlow()

    private val _receiveSuccess = MutableSharedFlow<Boolean>()
    val receiveSuccess: SharedFlow<Boolean> = _receiveSuccess.asSharedFlow()

    init {
        initialProductId?.let { loadProductName(it) }
    }

    fun onProductIdChange(idStr: String) {
        _formState.update { it.copy(productId = idStr, productName = null) }
        val id = idStr.toLongOrNull()
        if (id != null && id > 0) {
            loadProductName(id)
        }
    }

    private fun loadProductName(id: Long) {
        viewModelScope.launch {
            _formState.update { it.copy(isFetchingProduct = true) }
            when (val result = productRepository.getProductById(id)) {
                is NetworkResult.Success -> {
                    _formState.update {
                        it.copy(
                            isFetchingProduct = false,
                            productName = result.data.name
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _formState.update { it.copy(isFetchingProduct = false, productName = null) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onQuantityChange(qty: String) = _formState.update { it.copy(quantity = qty) }
    fun onCostPriceChange(price: String) = _formState.update { it.copy(costPrice = price) }
    fun onExpiryDateChange(date: String) = _formState.update { it.copy(expiryDate = date) }

    fun submitBatch() {
        val state = _formState.value
        val productId = state.productId.toLongOrNull()
        if (productId == null || productId <= 0) {
            _formState.update { it.copy(errorMessage = "Vui lòng nhập ID sản phẩm hợp lệ") }
            return
        }

        val qty = state.quantity.toIntOrNull()
        if (qty == null || qty <= 0) {
            _formState.update { it.copy(errorMessage = "Số lượng nhập phải lớn hơn hoặc bằng 1") }
            return
        }

        val costPrice = state.costPrice.toDoubleOrNull()
        if (costPrice == null || costPrice < 0) {
            _formState.update { it.copy(errorMessage = "Giá vốn không hợp lệ") }
            return
        }

        val request = BatchRequest(
            productId = productId,
            quantity = qty,
            costPrice = costPrice,
            expiryDate = state.expiryDate.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = adminRepository.receiveBatch(request)) {
                is NetworkResult.Success -> {
                    _formState.update { it.copy(isLoading = false) }
                    _receiveSuccess.emit(true)
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

