package com.hieu.ui.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hieu.core.util.NetworkResult
import com.hieu.data.local.TokenManager
import com.hieu.data.remote.dto.UserDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    val currentUsername: String? = tokenManager.getUsername()

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val usersStream: Flow<PagingData<UserDto>> = _searchKeyword
        .debounce(300)
        .flatMapLatest { keyword ->
            adminRepository.getUsersStream(keyword.takeIf { it.isNotBlank() })
        }
        .cachedIn(viewModelScope)

    fun onKeywordChanged(keyword: String) {
        _searchKeyword.value = keyword
    }

    fun toggleUserStatus(user: UserDto) {
        if (user.username == currentUsername) {
            _toastMessage.value = "Không thể tự khóa tài khoản của chính mình"
            return
        }
        val userId = user.id ?: return
        val newStatus = !(user.status ?: true)

        viewModelScope.launch {
            when (val result = adminRepository.updateUserStatus(userId, newStatus)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = if (newStatus) "Đã kích hoạt người dùng" else "Đã khóa người dùng"
                }
                is NetworkResult.Error -> {
                    _toastMessage.value = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteUser(user: UserDto) {
        if (user.username == currentUsername) {
            _toastMessage.value = "Không thể tự xóa tài khoản của chính mình"
            return
        }
        val userId = user.id ?: return

        viewModelScope.launch {
            when (val result = adminRepository.deleteUser(userId)) {
                is NetworkResult.Success -> {
                    _toastMessage.value = "Đã xóa người dùng thành công"
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

