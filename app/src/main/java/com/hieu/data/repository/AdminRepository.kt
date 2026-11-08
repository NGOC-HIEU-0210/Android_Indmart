package com.hieu.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.safeApiCall
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.dto.*
import com.hieu.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

interface AdminRepository {
    // 1. Dashboard
    suspend fun getDashboardStats(): NetworkResult<DashboardDto>

    // 2. Sản phẩm
    fun getAdminProductsStream(): Flow<PagingData<ProductDto>>
    suspend fun createProduct(product: ProductDto): NetworkResult<ProductDto>
    suspend fun updateProduct(id: Long, product: ProductDto): NetworkResult<ProductDto>
    suspend fun uploadProductMainImage(id: Long, file: MultipartBody.Part): NetworkResult<ProductDto>
    suspend fun uploadProductAdditionalImage(id: Long, file: MultipartBody.Part): NetworkResult<ProductImageDto>
    suspend fun deleteProductImage(imageId: Long): NetworkResult<Unit>
    suspend fun deleteProduct(id: Long): NetworkResult<Unit>

    // 3. Danh mục
    suspend fun getAllCategoriesAdmin(keyword: String? = null, page: Int = 0, size: Int = 50): NetworkResult<PageResponse<CategoryDto>>
    suspend fun createCategory(category: CategoryDto): NetworkResult<CategoryDto>
    suspend fun updateCategory(id: Long, category: CategoryDto): NetworkResult<CategoryDto>
    suspend fun uploadCategoryImage(id: Long, file: MultipartBody.Part): NetworkResult<CategoryDto>
    suspend fun deleteCategory(id: Long): NetworkResult<Unit>

    // 4. Coupon
    suspend fun getCouponsPaged(page: Int = 0, size: Int = 20): NetworkResult<PageResponse<CouponDto>>
    suspend fun getCouponById(id: Long): NetworkResult<CouponDto>
    suspend fun createCoupon(coupon: CouponDto): NetworkResult<CouponDto>
    suspend fun updateCoupon(id: Long, coupon: CouponDto): NetworkResult<CouponDto>
    suspend fun deleteCoupon(id: Long): NetworkResult<Unit>

    // 5. Đơn hàng
    fun getAdminOrdersStream(statusId: Long? = null, keyword: String? = null): Flow<PagingData<OrderDto>>
    suspend fun updateOrderStatus(id: Long, statusId: Long): NetworkResult<OrderDto>
    suspend fun updateOrderPaymentStatus(id: Long, paymentStatus: String): NetworkResult<OrderDto>
    suspend fun deleteOrder(id: Long): NetworkResult<Unit>
    suspend fun getOrderStatuses(): NetworkResult<List<OrderStatusDto>>

    // 6. Kho / Lô hàng (Batches & Inventory)
    suspend fun receiveBatch(request: BatchRequest): NetworkResult<BatchDto>
    suspend fun getBatchesByProduct(productId: Long, page: Int = 0, size: Int = 20): NetworkResult<PageResponse<BatchDto>>
    suspend fun getExpiringBatches(days: Int = 3, page: Int = 0, size: Int = 50): NetworkResult<PageResponse<BatchDto>>
    suspend fun getWasteReport(from: String? = null, to: String? = null): NetworkResult<WasteReportDto>
    suspend fun writeOffExpiredBatches(): NetworkResult<Int>

    // 7. Quản lý Người dùng (Users)
    fun getUsersStream(keyword: String? = null): Flow<PagingData<UserDto>>
    suspend fun getUserById(id: Long): NetworkResult<UserDto>
    suspend fun createUser(request: RegisterRequest): NetworkResult<UserDto>
    suspend fun updateUser(id: Long, dto: UserDto): NetworkResult<UserDto>
    suspend fun updateUserStatus(id: Long, status: Boolean): NetworkResult<UserDto>
    suspend fun uploadUserAvatar(id: Long, file: MultipartBody.Part): NetworkResult<UserDto>
    suspend fun deleteUser(id: Long): NetworkResult<Unit>
}

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val apiService: IndMartApiService,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AdminRepository {

    // 1. Dashboard
    override suspend fun getDashboardStats(): NetworkResult<DashboardDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getDashboardStats()
        }
    }

    // 2. Sản phẩm
    override fun getAdminProductsStream(): Flow<PagingData<ProductDto>> {
        return Pager(
            config = PagingConfig(pageSize = 15, enablePlaceholders = false),
            pagingSourceFactory = {
                ProductPagingSource { page, size ->
                    val result = apiService.getAllProductsAdmin(page, size)
                    if (result.success && result.data != null) {
                        result.data
                    } else {
                        throw Exception(result.message ?: "Lỗi tải danh sách sản phẩm quản trị")
                    }
                }
            }
        ).flow
    }

    override suspend fun createProduct(product: ProductDto): NetworkResult<ProductDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.createProduct(product)
        }
    }

    override suspend fun updateProduct(id: Long, product: ProductDto): NetworkResult<ProductDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateProduct(id, product)
        }
    }

    override suspend fun uploadProductMainImage(id: Long, file: MultipartBody.Part): NetworkResult<ProductDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.uploadProductMainImage(id, file)
        }
    }

    override suspend fun uploadProductAdditionalImage(id: Long, file: MultipartBody.Part): NetworkResult<ProductImageDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.uploadProductAdditionalImage(id, file)
        }
    }

    override suspend fun deleteProductImage(imageId: Long): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.deleteProductImage(imageId)
        }
    }

    override suspend fun deleteProduct(id: Long): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.deleteProduct(id)
        }
    }

    // 3. Danh mục
    override suspend fun getAllCategoriesAdmin(keyword: String?, page: Int, size: Int): NetworkResult<PageResponse<CategoryDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getAllCategoriesAdmin(keyword, page, size)
        }
    }

    override suspend fun createCategory(category: CategoryDto): NetworkResult<CategoryDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.createCategory(category)
        }
    }

    override suspend fun updateCategory(id: Long, category: CategoryDto): NetworkResult<CategoryDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateCategory(id, category)
        }
    }

    override suspend fun uploadCategoryImage(id: Long, file: MultipartBody.Part): NetworkResult<CategoryDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.uploadCategoryImage(id, file)
        }
    }

    override suspend fun deleteCategory(id: Long): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.deleteCategory(id)
        }
    }

    // 4. Coupon
    override suspend fun getCouponsPaged(page: Int, size: Int): NetworkResult<PageResponse<CouponDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getCouponsPaged(page, size)
        }
    }

    override suspend fun getCouponById(id: Long): NetworkResult<CouponDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getCouponById(id)
        }
    }

    override suspend fun createCoupon(coupon: CouponDto): NetworkResult<CouponDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.createCoupon(coupon)
        }
    }

    override suspend fun updateCoupon(id: Long, coupon: CouponDto): NetworkResult<CouponDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateCoupon(id, coupon)
        }
    }

    override suspend fun deleteCoupon(id: Long): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.deleteCoupon(id)
        }
    }

    // 5. Đơn hàng
    override fun getAdminOrdersStream(statusId: Long?, keyword: String?): Flow<PagingData<OrderDto>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                OrderPagingSource { page, size ->
                    val result = if (statusId != null || !keyword.isNullOrBlank()) {
                        apiService.searchOrders(statusId = statusId, keyword = keyword, page = page, size = size)
                    } else {
                        apiService.getOrdersPaged(page = page, size = size)
                    }
                    if (result.success && result.data != null) {
                        result.data
                    } else {
                        throw Exception(result.message ?: "Lỗi tải danh sách đơn hàng quản trị")
                    }
                }
            }
        ).flow
    }

    override suspend fun updateOrderStatus(id: Long, statusId: Long): NetworkResult<OrderDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateOrderStatus(id, statusId)
        }
    }

    override suspend fun updateOrderPaymentStatus(id: Long, paymentStatus: String): NetworkResult<OrderDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateOrderPaymentStatus(id, paymentStatus)
        }
    }

    override suspend fun deleteOrder(id: Long): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.deleteOrder(id)
        }
    }

    override suspend fun getOrderStatuses(): NetworkResult<List<OrderStatusDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getOrderStatuses()
        }
    }

    // 6. Kho / Lô hàng (Batches & Inventory)
    override suspend fun receiveBatch(request: BatchRequest): NetworkResult<BatchDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.receiveBatch(request)
        }
    }

    override suspend fun getBatchesByProduct(productId: Long, page: Int, size: Int): NetworkResult<PageResponse<BatchDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getBatchesByProduct(productId, page, size)
        }
    }

    override suspend fun getExpiringBatches(days: Int, page: Int, size: Int): NetworkResult<PageResponse<BatchDto>> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getExpiringBatches(days, page, size)
        }
    }

    override suspend fun getWasteReport(from: String?, to: String?): NetworkResult<WasteReportDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getWasteReport(from, to)
        }
    }

    override suspend fun writeOffExpiredBatches(): NetworkResult<Int> {
        return safeApiCall(ioDispatcher, json) {
            apiService.writeOffExpiredBatches()
        }
    }

    // 7. Quản lý Người dùng (Users)
    override fun getUsersStream(keyword: String?): Flow<PagingData<UserDto>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                UserPagingSource { page, size ->
                    val result = if (!keyword.isNullOrBlank()) {
                        apiService.searchUsers(keyword = keyword, page = page, size = size)
                    } else {
                        apiService.getUsersPaged(page = page, size = size)
                    }
                    if (result.success && result.data != null) {
                        result.data
                    } else {
                        throw Exception(result.message ?: "Lỗi tải danh sách người dùng")
                    }
                }
            }
        ).flow
    }

    override suspend fun getUserById(id: Long): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.getUserById(id)
        }
    }

    override suspend fun createUser(request: RegisterRequest): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.createUser(request)
        }
    }

    override suspend fun updateUser(id: Long, dto: UserDto): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateUser(id, dto)
        }
    }

    override suspend fun updateUserStatus(id: Long, status: Boolean): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.updateUserStatus(id, status)
        }
    }

    override suspend fun uploadUserAvatar(id: Long, file: MultipartBody.Part): NetworkResult<UserDto> {
        return safeApiCall(ioDispatcher, json) {
            apiService.uploadUserAvatar(id, file)
        }
    }

    override suspend fun deleteUser(id: Long): NetworkResult<Unit> {
        return safeApiCall(ioDispatcher, json) {
            apiService.deleteUser(id)
        }
    }
}

class OrderPagingSource(
    private val fetchPage: suspend (page: Int, size: Int) -> PageResponse<OrderDto>
) : androidx.paging.PagingSource<Int, OrderDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OrderDto> {
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

    override fun getRefreshKey(state: androidx.paging.PagingState<Int, OrderDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class UserPagingSource(
    private val fetchPage: suspend (page: Int, size: Int) -> PageResponse<UserDto>
) : androidx.paging.PagingSource<Int, UserDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserDto> {
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

    override fun getRefreshKey(state: androidx.paging.PagingState<Int, UserDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
