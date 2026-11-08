package com.hieu.ui.admin.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CategoryDto
import com.hieu.data.remote.dto.ProductDto
import com.hieu.data.remote.dto.ProductImageDto
import com.hieu.data.repository.AdminRepository
import com.hieu.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class AdminProductEditUiState(
    val isNew: Boolean = true,
    val id: Long = 0L,
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val categoryId: Long? = null,
    val shelfLifeDays: String = "",
    val active: Boolean = true,
    val mainImage: String? = null,
    val images: List<ProductImageDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingImage: Boolean = false,
    val nameError: String? = null,
    val priceError: String? = null,
    val toastMessage: String? = null
)

@HiltViewModel
class AdminProductEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val adminRepository: AdminRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    private val _uiState = MutableStateFlow(AdminProductEditUiState(isNew = productId <= 0L, id = productId))
    val uiState: StateFlow<AdminProductEditUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        if (productId > 0L) {
            loadProductData(productId)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = productRepository.getActiveCategories(size = 50)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(categories = result.data.content) }
                }
                else -> Unit
            }
        }
    }

    private fun loadProductData(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = productRepository.getProductById(id)) {
                is NetworkResult.Success -> {
                    val p = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = p.name,
                            price = "${p.price.toLong()}",
                            description = p.description ?: "",
                            categoryId = p.categoryId,
                            shelfLifeDays = p.shelfLifeDays?.toString() ?: "",
                            active = p.active ?: true,
                            mainImage = p.image,
                            images = p.images
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun onPriceChange(v: String) = _uiState.update { it.copy(price = v, priceError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onCategoryChange(id: Long?) = _uiState.update { it.copy(categoryId = id) }
    fun onShelfLifeDaysChange(v: String) = _uiState.update { it.copy(shelfLifeDays = v) }
    fun onActiveToggle(v: Boolean) = _uiState.update { it.copy(active = v) }

    fun saveProduct(onSuccess: (Long) -> Unit) {
        val s = _uiState.value
        val name = s.name.trim()
        val priceDouble = s.price.toDoubleOrNull()

        var hasError = false
        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "Tên sản phẩm không được để trống") }
            hasError = true
        }
        if (priceDouble == null || priceDouble < 0) {
            _uiState.update { it.copy(priceError = "Giá bán không hợp lệ") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val productDto = ProductDto(
                id = if (s.isNew) null else s.id,
                name = name,
                price = priceDouble ?: 0.0,
                description = s.description.trim().ifBlank { null },
                categoryId = s.categoryId,
                shelfLifeDays = s.shelfLifeDays.toIntOrNull(),
                active = s.active
            )

            val result = if (s.isNew) {
                adminRepository.createProduct(productDto)
            } else {
                adminRepository.updateProduct(s.id, productDto)
            }

            when (result) {
                is NetworkResult.Success -> {
                    val saved = result.data
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            id = saved.id ?: s.id,
                            isNew = false,
                            toastMessage = "Đã lưu sản phẩm thành công!"
                        )
                    }
                    saved.id?.let(onSuccess)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun uploadMainImage(context: Context, uri: Uri) {
        val currentId = _uiState.value.id
        if (currentId <= 0L) {
            _uiState.update { it.copy(toastMessage = "Vui lòng lưu thông tin sản phẩm trước khi tải ảnh!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }
            val part = createMultipartPart(context, uri) ?: run {
                _uiState.update { it.copy(isUploadingImage = false, toastMessage = "Không đọc được file ảnh") }
                return@launch
            }

            when (val result = adminRepository.uploadProductMainImage(currentId, part)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            mainImage = result.data.image,
                            toastMessage = "Đã cập nhật ảnh đại diện"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isUploadingImage = false, toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun uploadAdditionalImage(context: Context, uri: Uri) {
        val currentId = _uiState.value.id
        if (currentId <= 0L) {
            _uiState.update { it.copy(toastMessage = "Vui lòng lưu sản phẩm trước khi thêm ảnh phụ!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }
            val part = createMultipartPart(context, uri) ?: run {
                _uiState.update { it.copy(isUploadingImage = false, toastMessage = "Không đọc được file ảnh") }
                return@launch
            }

            when (val result = adminRepository.uploadProductAdditionalImage(currentId, part)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            images = it.images + result.data,
                            toastMessage = "Đã thêm ảnh phụ"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isUploadingImage = false, toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteAdditionalImage(imageId: Long) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteProductImage(imageId)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            images = it.images.filterNot { img -> img.id == imageId },
                            toastMessage = "Đã xóa ảnh phụ"
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

    private fun createMultipartPart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val stream = context.contentResolver.openInputStream(uri)
            val bytes = stream?.readBytes()
            stream?.close()
            if (bytes != null) {
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", "image.jpg", body)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun clearToast() = _uiState.update { it.copy(toastMessage = null) }
}

