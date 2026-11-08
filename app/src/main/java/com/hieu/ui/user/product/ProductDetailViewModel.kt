package com.hieu.ui.user.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.local.TokenManager
import com.hieu.data.remote.dto.ProductDto
import com.hieu.data.repository.CartRepository
import com.hieu.data.repository.FavouriteRepository
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

data class ProductDetailUiState(
    val productState: UiState<ProductDto> = UiState.Loading,
    val quantity: Int = 1,
    val isFavourite: Boolean = false,
    val isAddingToCart: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val favouriteRepository: FavouriteRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _cartAddedEvent = MutableSharedFlow<Unit>()
    val cartAddedEvent: SharedFlow<Unit> = _cartAddedEvent.asSharedFlow()

    init {
        loadProductDetail()
        checkFavouriteStatus()
    }

    fun loadProductDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(productState = UiState.Loading) }
            when (val result = productRepository.getProductById(productId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(productState = UiState.Success(result.data)) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(productState = UiState.Error(result.message)) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun checkFavouriteStatus() {
        if (!tokenManager.isLoggedIn()) return
        viewModelScope.launch {
            when (val result = favouriteRepository.checkFavourite(productId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isFavourite = result.data) }
                }
                else -> Unit
            }
        }
    }

    fun toggleFavourite() {
        if (!tokenManager.isLoggedIn()) {
            _uiState.update { it.copy(toastMessage = "Vui lòng đăng nhập để lưu sản phẩm yêu thích") }
            return
        }

        viewModelScope.launch {
            when (val result = favouriteRepository.toggleFavourite(productId)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isFavourite = result.data,
                            toastMessage = if (result.data) "Đã thêm vào yêu thích" else "Đã xóa khỏi yêu thích"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun incrementQuantity() {
        val currentProduct = (_uiState.value.productState as? UiState.Success)?.data
        val maxStock = currentProduct?.stock ?: 99
        if (_uiState.value.quantity < maxStock) {
            _uiState.update { it.copy(quantity = it.quantity + 1) }
        }
    }

    fun decrementQuantity() {
        if (_uiState.value.quantity > 1) {
            _uiState.update { it.copy(quantity = it.quantity - 1) }
        }
    }

    fun addToCart(onRequireLogin: () -> Unit) {
        if (!tokenManager.isLoggedIn()) {
            onRequireLogin()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCart = true) }
            when (val result = cartRepository.addToCart(productId, _uiState.value.quantity)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAddingToCart = false,
                            toastMessage = "Đã thêm vào giỏ hàng thành công!"
                        )
                    }
                    _cartAddedEvent.emit(Unit)
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAddingToCart = false,
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

