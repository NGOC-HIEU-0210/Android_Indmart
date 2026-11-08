package com.hieu.core.util

import com.hieu.data.remote.dto.ApiResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    json: Json = Json { ignoreUnknownKeys = true },
    apiCall: suspend () -> ApiResponse<T>
): NetworkResult<T> {
    return withContext(dispatcher) {
        try {
            val response = apiCall()
            if (response.success) {
                // Với các API void (như delete, clearCart), data có thể là null hoặc Unit
                @Suppress("UNCHECKED_CAST")
                NetworkResult.Success(response.data as T)
            } else {
                NetworkResult.Error(
                    message = response.message ?: "Yêu cầu thất bại",
                    code = null
                )
            }
        } catch (e: HttpException) {
            val code = e.code()
            if (code == 429) {
                val retryAfter = e.response()?.headers()?.get("Retry-After")
                val msg = if (!retryAfter.isNullOrBlank()) {
                    "Quá nhiều yêu cầu. Vui lòng thử lại sau $retryAfter giây."
                } else {
                    "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
                }
                return@withContext NetworkResult.Error(message = msg, code = code)
            }

            val errorMsg = parseErrorMessage(e.response()?.errorBody(), json)
                ?: when (code) {
                    401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                    403 -> "Bạn không có quyền thực hiện thao tác này."
                    404 -> "Không tìm thấy dữ liệu yêu cầu."
                    500 -> "Lỗi máy chủ nội bộ. Vui lòng thử lại sau."
                    else -> "Lỗi máy chủ: HTTP $code"
                }

            NetworkResult.Error(message = errorMsg, code = code)
        } catch (_: IOException) {
            NetworkResult.Error(
                message = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng."
            )
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.localizedMessage ?: "Đã xảy ra lỗi không xác định."
            )
        }
    }
}

private fun parseErrorMessage(errorBody: ResponseBody?, json: Json): String? {
    if (errorBody == null) return null
    return try {
        val string = errorBody.string()
        val errorResponse = json.decodeFromString<ApiResponse<String?>>(string)
        errorResponse.message
    } catch (_: Exception) {
        null
    }
}

