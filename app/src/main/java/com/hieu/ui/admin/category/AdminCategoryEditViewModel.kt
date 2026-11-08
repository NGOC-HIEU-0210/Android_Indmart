package com.hieu.ui.admin.category

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.data.remote.dto.CategoryDto
import com.hieu.data.repository.AdminRepository
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

data class AdminCategoryEditUiState(
    val isNew: Boolean = true,
    val id: Long = 0L,
    val name: String = "",
    val active: Boolean = true,
    val parentCategoryId: Long? = null,
    val image: String? = null,
    val categories: List<CategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingImage: Boolean = false,
    val nameError: String? = null,
    val toastMessage: String? = null
)

@HiltViewModel
class AdminCategoryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L

    private val _uiState = MutableStateFlow(AdminCategoryEditUiState(isNew = categoryId <= 0L, id = categoryId))
    val uiState: StateFlow<AdminCategoryEditUiState> = _uiState.asStateFlow()

    init {
        loadAllCategories()
    }

    private fun loadAllCategories() {
        viewModelScope.launch {
            when (val result = adminRepository.getAllCategoriesAdmin(size = 50)) {
                is NetworkResult.Success -> {
                    val list = result.data.content
                    _uiState.update { it.copy(categories = list) }

                    // Nếu chỉnh sửa thì tìm category tương ứng
                    if (categoryId > 0L) {
                        val current = list.find { it.id == categoryId }
                        if (current != null) {
                            _uiState.update {
                                it.copy(
                                    name = current.name,
                                    active = current.active ?: true,
                                    parentCategoryId = current.parentCategoryId,
                                    image = current.image
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun onActiveToggle(v: Boolean) = _uiState.update { it.copy(active = v) }
    fun onParentCategoryChange(id: Long?) = _uiState.update { it.copy(parentCategoryId = id) }

    fun saveCategory(onSuccess: () -> Unit) {
        val s = _uiState.value
        val name = s.name.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "Tên danh mục không được để trống") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val dto = CategoryDto(
                id = if (s.isNew) null else s.id,
                name = name,
                active = s.active,
                parentCategoryId = s.parentCategoryId
            )

            val result = if (s.isNew) {
                adminRepository.createCategory(dto)
            } else {
                adminRepository.updateCategory(s.id, dto)
            }

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            id = result.data.id ?: s.id,
                            isNew = false,
                            toastMessage = "Đã lưu danh mục thành công!"
                        )
                    }
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, toastMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun uploadImage(context: Context, uri: Uri) {
        val currentId = _uiState.value.id
        if (currentId <= 0L) {
            _uiState.update { it.copy(toastMessage = "Vui lòng lưu thông tin danh mục trước khi tải ảnh!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }
            val part = try {
                val stream = context.contentResolver.openInputStream(uri)
                val bytes = stream?.readBytes()
                stream?.close()
                if (bytes != null) {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", "cat.jpg", body)
                } else null
            } catch (_: Exception) {
                null
            }

            if (part == null) {
                _uiState.update { it.copy(isUploadingImage = false, toastMessage = "Không đọc được file ảnh") }
                return@launch
            }

            when (val result = adminRepository.uploadCategoryImage(currentId, part)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            image = result.data.image,
                            toastMessage = "Đã tải lên ảnh danh mục"
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

    fun clearToast() = _uiState.update { it.copy(toastMessage = null) }
}

