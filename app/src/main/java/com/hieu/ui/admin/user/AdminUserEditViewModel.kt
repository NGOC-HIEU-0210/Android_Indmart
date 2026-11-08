package com.hieu.ui.admin.user

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.data.remote.dto.RegisterRequest
import com.hieu.data.remote.dto.UserDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class AdminUserFormState(
    val isNew: Boolean = true,
    val id: Long? = null,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val mobile: String = "",
    val address: String = "",
    val avatar: String? = null,
    val status: Boolean = true,
    val roles: List<String> = listOf("USER"),
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminUserEditViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long? = savedStateHandle.get<Long>("userId")?.takeIf { it > 0 }

    private val _formState = MutableStateFlow(
        AdminUserFormState(isNew = userId == null, id = userId)
    )
    val formState: StateFlow<AdminUserFormState> = _formState.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess: SharedFlow<Boolean> = _saveSuccess.asSharedFlow()

    init {
        userId?.let { loadUserData(it) }
    }

    private fun loadUserData(id: Long) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getUserById(id)) {
                is NetworkResult.Success -> {
                    val u = result.data
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            username = u.username ?: "",
                            email = u.email ?: "",
                            firstName = u.firstName ?: "",
                            lastName = u.lastName ?: "",
                            mobile = u.mobile ?: "",
                            address = u.address ?: "",
                            avatar = u.avatar,
                            status = u.status ?: true,
                            roles = if (u.roles.isNotEmpty()) u.roles else listOf("USER")
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun onUsernameChange(v: String) = _formState.update { it.copy(username = v) }
    fun onEmailChange(v: String) = _formState.update { it.copy(email = v) }
    fun onPasswordChange(v: String) = _formState.update { it.copy(password = v) }
    fun onFirstNameChange(v: String) = _formState.update { it.copy(firstName = v) }
    fun onLastNameChange(v: String) = _formState.update { it.copy(lastName = v) }
    fun onMobileChange(v: String) = _formState.update { it.copy(mobile = v) }
    fun onAddressChange(v: String) = _formState.update { it.copy(address = v) }
    fun onStatusChange(v: Boolean) = _formState.update { it.copy(status = v) }

    fun toggleRole(role: String) {
        _formState.update {
            val updated = if (it.roles.contains(role)) {
                if (it.roles.size > 1) it.roles - role else it.roles
            } else {
                it.roles + role
            }
            it.copy(roles = updated)
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        val targetId = _formState.value.id ?: return
        viewModelScope.launch {
            try {
                _formState.update { it.copy(isUploadingAvatar = true) }
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                inputStream.close()

                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "avatar_${System.currentTimeMillis()}.jpg", requestBody)

                when (val result = adminRepository.uploadUserAvatar(targetId, part)) {
                    is NetworkResult.Success -> {
                        _formState.update {
                            it.copy(
                                isUploadingAvatar = false,
                                avatar = result.data.avatar
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _formState.update { it.copy(isUploadingAvatar = false, errorMessage = result.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            } catch (e: Exception) {
                _formState.update { it.copy(isUploadingAvatar = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    fun saveUser() {
        val s = _formState.value
        if (s.username.isBlank()) {
            _formState.update { it.copy(errorMessage = "Tên đăng nhập không được để trống") }
            return
        }
        if (s.email.isBlank()) {
            _formState.update { it.copy(errorMessage = "Email không được để trống") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }
            if (s.isNew) {
                if (s.password.length < 6) {
                    _formState.update { it.copy(isLoading = false, errorMessage = "Mật khẩu tối thiểu 6 ký tự") }
                    return@launch
                }
                val request = RegisterRequest(
                    username = s.username.trim(),
                    email = s.email.trim(),
                    password = s.password,
                    firstName = s.firstName.takeIf { it.isNotBlank() },
                    lastName = s.lastName.takeIf { it.isNotBlank() },
                    mobile = s.mobile.takeIf { it.isNotBlank() },
                    address = s.address.takeIf { it.isNotBlank() }
                )
                when (val result = adminRepository.createUser(request)) {
                    is NetworkResult.Success -> {
                        _formState.update { it.copy(isLoading = false) }
                        _saveSuccess.emit(true)
                    }
                    is NetworkResult.Error -> {
                        _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            } else {
                val dto = UserDto(
                    id = s.id,
                    username = s.username.trim(),
                    email = s.email.trim(),
                    firstName = s.firstName.takeIf { it.isNotBlank() },
                    lastName = s.lastName.takeIf { it.isNotBlank() },
                    mobile = s.mobile.takeIf { it.isNotBlank() },
                    address = s.address.takeIf { it.isNotBlank() },
                    avatar = s.avatar,
                    status = s.status,
                    roles = s.roles
                )
                when (val result = adminRepository.updateUser(s.id!!, dto)) {
                    is NetworkResult.Success -> {
                        _formState.update { it.copy(isLoading = false) }
                        _saveSuccess.emit(true)
                    }
                    is NetworkResult.Error -> {
                        _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun clearError() = _formState.update { it.copy(errorMessage = null) }
}

