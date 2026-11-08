package com.hieu.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.safeApiCall
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.dto.CategoryDto
import com.hieu.data.remote.dto.PageResponse
import com.hieu.data.remote.dto.ProductDto
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface ProductRepository {
    suspend fun getRescueProducts(): NetworkResult<List<ProductDto>>
    suspend fun getLatestProducts(page: Int = 0, size: Int = 8): NetworkResult<PageResponse<ProductDto>>
    fun getLatestProductsStream(): Flow<PagingData<ProductDto>>
    fun searchProductsStream(keyword: String? = null, categoryId: Long? = null): Flow<PagingData<ProductDto>>
    fun getProductsByCategoryStream(categoryId: Long): Flow<PagingData<ProductDto>>
    suspend fun getProductById(id: Long): NetworkResult<ProductDto>
    suspend fun getActiveCategories(keyword: String? = null, page: Int = 0, size: Int = 50): NetworkResult<PageResponse<CategoryDto>>
    suspend fun getCategoryById(id: Long): NetworkResult<CategoryDto>
}

class ProductPagingSource(
    private val fetchPage: suspend (page: Int, size: Int) -> PageResponse<ProductDto>
) : PagingSource<Int, ProductDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductDto> {
        val page = params.key ?: 0
        return try {
            val response = fetchPage(page, params.loadSize)
            LoadResult.Page(
                data = response.content,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.last || response.content.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ProductDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: IndMartApiService,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override suspend fun getRescueProducts(): NetworkResult<List<ProductDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getRescueProducts()
        }
    }

    override suspend fun getLatestProducts(page: Int, size: Int): NetworkResult<PageResponse<ProductDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getLatestProducts(page, size)
        }
    }

    override fun getLatestProductsStream(): Flow<PagingData<ProductDto>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                ProductPagingSource { page, size ->
                    val result = apiService.getLatestProducts(page, size)
                    if (result.success && result.data != null) {
                        result.data
                    } else {
                        throw Exception(result.message ?: "Lỗi tải sản phẩm")
                    }
                }
            }
        ).flow
    }

    override fun searchProductsStream(keyword: String?, categoryId: Long?): Flow<PagingData<ProductDto>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                ProductPagingSource { page, size ->
                    val result = apiService.searchProducts(
                        keyword = keyword.takeIf { !it.isNullOrBlank() },
                        categoryId = categoryId,
                        page = page,
                        size = size
                    )
                    if (result.success && result.data != null) {
                        result.data
                    } else {
                        throw Exception(result.message ?: "Lỗi tìm kiếm sản phẩm")
                    }
                }
            }
        ).flow
    }

    override fun getProductsByCategoryStream(categoryId: Long): Flow<PagingData<ProductDto>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                ProductPagingSource { page, size ->
                    val result = apiService.getProductsByCategory(categoryId, page, size)
                    if (result.success && result.data != null) {
                        result.data
                    } else {
                        throw Exception(result.message ?: "Lỗi tải sản phẩm danh mục")
                    }
                }
            }
        ).flow
    }

    override suspend fun getProductById(id: Long): NetworkResult<ProductDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getProductById(id)
        }
    }

    override suspend fun getActiveCategories(keyword: String?, page: Int, size: Int): NetworkResult<PageResponse<CategoryDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getActiveCategories(keyword, page, size)
        }
    }

    override suspend fun getCategoryById(id: Long): NetworkResult<CategoryDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getCategoryById(id)
        }
    }
}

