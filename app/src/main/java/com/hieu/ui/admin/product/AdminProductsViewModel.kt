package com.hieu.ui.admin.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hieu.core.util.NetworkResult
import com.hieu.data.remote.dto.ProductDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminProductsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    val productsPagingFlow: Flow<PagingData<ProductDto>> = adminRepository
        .getAdminProductsStream()
        .cachedIn(viewModelScope)

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun deleteProduct(productId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteProduct(productId)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Đã xóa sản phẩm thành công"
                    onDeleted()
                }
                is NetworkResult.Error -> {
                    _toastMessage.value = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

