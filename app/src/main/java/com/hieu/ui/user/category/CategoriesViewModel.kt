package com.hieu.ui.user.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CategoryDto
import com.hieu.data.remote.dto.ProductDto
import com.hieu.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val productsPagingFlow: Flow<PagingData<ProductDto>> = combine(
        _selectedCategoryId,
        _searchQuery.debounce(300)
    ) { catId, query ->
        Pair(catId, query)
    }.flatMapLatest { (catId, query) ->
        if (catId != null) {
            productRepository.getProductsByCategoryStream(catId)
        } else if (query.isNotBlank()) {
            productRepository.searchProductsStream(keyword = query)
        } else {
            productRepository.getLatestProductsStream()
        }
    }.cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    fun loadCategories(keyword: String? = null) {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            when (val result = productRepository.getActiveCategories(keyword = keyword, size = 50)) {
                is NetworkResult.Success -> {
                    val list = result.data.content
                    if (list.isEmpty()) {
                        _categoriesState.value = UiState.Empty
                    } else {
                        _categoriesState.value = UiState.Success(list)
                        // Mặc định chọn danh mục đầu tiên nếu chưa chọn
                        if (_selectedCategoryId.value == null && list.isNotEmpty()) {
                            _selectedCategoryId.value = list.first().id
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _categoriesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }
}

