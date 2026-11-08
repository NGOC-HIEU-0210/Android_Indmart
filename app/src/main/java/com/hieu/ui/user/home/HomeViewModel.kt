package com.hieu.ui.user.home

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
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _rescueState = MutableStateFlow<UiState<List<ProductDto>>>(UiState.Loading)
    val rescueState: StateFlow<UiState<List<ProductDto>>> = _rescueState.asStateFlow()

    private val _categoriesState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val productsPagingFlow: Flow<PagingData<ProductDto>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                productRepository.getLatestProductsStream()
            } else {
                productRepository.searchProductsStream(keyword = query)
            }
        }
        .cachedIn(viewModelScope)

    init {
        loadData()
    }

    fun loadData() {
        loadRescueProducts()
        loadCategories()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun loadRescueProducts() {
        viewModelScope.launch {
            _rescueState.value = UiState.Loading
            when (val result = productRepository.getRescueProducts()) {
                is NetworkResult.Success -> {
                    if (result.data.isEmpty()) {
                        _rescueState.value = UiState.Empty
                    } else {
                        _rescueState.value = UiState.Success(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    _rescueState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            when (val result = productRepository.getActiveCategories(size = 30)) {
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
}

