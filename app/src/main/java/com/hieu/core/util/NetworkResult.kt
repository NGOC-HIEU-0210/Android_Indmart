package com.hieu.core.util

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>
    data object Loading : NetworkResult<Nothing>
}

inline fun <T> NetworkResult<T>.onSuccess(action: (value: T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (message: String, code: Int?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message, code)
    }
    return this
}

