package com.hieu.ui.user.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.local.TokenManager
import com.hieu.data.local.UserSession
import com.hieu.data.remote.dto.UserDto
import com.hieu.data.repository.AuthRepository
import com.hieu.data.repository.UserRepository
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

data class ProfileUiState(
    val userState: UiState<UserDto> = UiState.Loading,
    val firstName: String = "",
    val lastName: String = "",
    val mobile: String = "",
    val address: String = "",
    val isUpdating: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    val session: StateFlow<UserSession> = tokenManager.sessionState

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (!tokenManager.isLoggedIn()) {
            _uiState.update { it.copy(userState = UiState.Empty) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(userState = UiState.Loading) }
            when (val result = userRepository.getMyProfile()) {
                is NetworkResult.Success -> {
                    val user = result.data
                    _uiState.update {
                        it.copy(
                            userState = UiState.Success(user),
                            firstName = user.firstName ?: "",
                            lastName = user.lastName ?: "",
                            mobile = user.mobile ?: "",
                            address = user.address ?: ""
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(userState = UiState.Error(result.message)) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onFirstNameChange(name: String) = _uiState.update { it.copy(firstName = name) }
    fun onLastNameChange(name: String) = _uiState.update { it.copy(lastName = name) }
    fun onMobileChange(mobile: String) = _uiState.update { it.copy(mobile = mobile) }
    fun onAddressChange(address: String) = _uiState.update { it.copy(address = address) }

    fun updateProfile(onSuccess: () -> Unit) {
        val currentUser = (_uiState.value.userState as? UiState.Success)?.data ?: return
        val updatedUser = currentUser.copy(
            firstName = _uiState.value.firstName.trim(),
            lastName = _uiState.value.lastName.trim(),
            mobile = _uiState.value.mobile.trim(),
            address = _uiState.value.address.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = userRepository.updateMyProfile(updatedUser)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            userState = UiState.Success(result.data),
                            toastMessage = "Cập nhật hồ sơ thành công!"
                        )
                    }
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            toastMessage = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true) }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    _uiState.update {
                        it.copy(isUploadingAvatar = false, toastMessage = "Không thể đọc ảnh đã chọn")
                    }
                    return@launch
                }

                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "avatar.jpg", requestBody)

                when (val result = userRepository.uploadMyAvatar(part)) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploadingAvatar = false,
                                userState = UiState.Success(result.data),
                                toastMessage = "Cập nhật ảnh đại diện thành công!"
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(isUploadingAvatar = false, toastMessage = result.message)
                        }
                    }
                    is NetworkResult.Loading -> Unit
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isUploadingAvatar = false, toastMessage = "Lỗi xử lý ảnh: ${e.localizedMessage}")
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}

