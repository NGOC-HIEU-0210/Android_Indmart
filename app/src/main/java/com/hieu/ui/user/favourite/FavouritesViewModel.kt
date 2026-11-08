package com.hieu.ui.user.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.local.TokenManager
import com.hieu.data.remote.dto.FavouriteDto
import com.hieu.data.repository.FavouriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val favouriteRepository: FavouriteRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _favouritesState = MutableStateFlow<UiState<List<FavouriteDto>>>(UiState.Loading)
    val favouritesState: StateFlow<UiState<List<FavouriteDto>>> = _favouritesState.asStateFlow()

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    init {
        loadFavourites()
    }

    fun loadFavourites() {
        if (!tokenManager.isLoggedIn()) {
            _favouritesState.value = UiState.Empty
            return
        }

        viewModelScope.launch {
            _favouritesState.value = UiState.Loading
            when (val result = favouriteRepository.getFavourites()) {
                is NetworkResult.Success -> {
                    if (result.data.isEmpty()) {
                        _favouritesState.value = UiState.Empty
                    } else {
                        _favouritesState.value = UiState.Success(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    _favouritesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun removeFavourite(productId: Long) {
        viewModelScope.launch {
            when (favouriteRepository.removeFavourite(productId)) {
                is NetworkResult.Success -> {
                    val currentList = (_favouritesState.value as? UiState.Success)?.data ?: emptyList()
                    val updated = currentList.filterNot { it.productId == productId }
                    _favouritesState.value = if (updated.isEmpty()) UiState.Empty else UiState.Success(updated)
                }
                else -> Unit
            }
        }
    }
}

