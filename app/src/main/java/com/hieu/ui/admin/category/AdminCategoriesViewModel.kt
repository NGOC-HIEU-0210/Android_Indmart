package com.hieu.ui.admin.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CategoryDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCategoriesViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories(keyword: String? = null) {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            when (val result = adminRepository.getAllCategoriesAdmin(keyword = keyword, size = 50)) {
                is NetworkResult.Success -> {
                    val list = result.data.content
                    if (list.isEmpty()) {
                        _categoriesState.value = UiState.Empty
                    } else {
                        _categoriesState.value = UiState.Success(list)
                    }
                }
                is NetworkResult.Error -> {
                    _categoriesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteCategory(id)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Đã xóa danh mục thành công"
                    loadCategories()
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

