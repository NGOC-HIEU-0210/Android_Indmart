package com.hieu.data.remote.api

import com.hieu.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface IndMartApiService {

    // ==========================================
    // 1. SẢN PHẨM (PRODUCTS)
    // ==========================================

    @GET("api/products/latest")
    suspend fun getLatestProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 8
    ): ApiResponse<PageResponse<ProductDto>>

    @GET("api/products/rescue")
    suspend fun getRescueProducts(): ApiResponse<List<ProductDto>>

    @GET("api/products/search")
    suspend fun searchProducts(
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<ProductDto>>

    @GET("api/products/category/{categoryId}")
    suspend fun getProductsByCategory(
        @Path("categoryId") categoryId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<ProductDto>>

    @GET("api/products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): ApiResponse<ProductDto>

    // Admin Sản phẩm
    @GET("api/products")
    suspend fun getAllProductsAdmin(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<ProductDto>>

    @POST("api/products")
    suspend fun createProduct(
        @Body product: ProductDto
    ): ApiResponse<ProductDto>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body product: ProductDto
    ): ApiResponse<ProductDto>

    @Multipart
    @POST("api/products/{id}/image")
    suspend fun uploadProductMainImage(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): ApiResponse<ProductDto>

    @Multipart
    @POST("api/products/{id}/images")
    suspend fun uploadProductAdditionalImage(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): ApiResponse<ProductImageDto>

    @DELETE("api/products/images/{imageId}")
    suspend fun deleteProductImage(
        @Path("imageId") imageId: Long
    ): ApiResponse<Unit>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    // ==========================================
    // 2. DANH MỤC (CATEGORIES)
    // ==========================================

    @GET("api/categories/active")
    suspend fun getActiveCategories(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<CategoryDto>>

    @GET("api/categories")
    suspend fun getAllCategoriesAdmin(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<CategoryDto>>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: Long
    ): ApiResponse<CategoryDto>

    @POST("api/categories")
    suspend fun createCategory(
        @Body category: CategoryDto
    ): ApiResponse<CategoryDto>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body category: CategoryDto
    ): ApiResponse<CategoryDto>

    @Multipart
    @POST("api/categories/{id}/image")
    suspend fun uploadCategoryImage(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): ApiResponse<CategoryDto>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    // ==========================================
    // 3. GIỎ HÀNG (CART)
    // ==========================================

    @GET("api/cart")
    suspend fun getCart(): ApiResponse<CartDto>

    @POST("api/cart/items")
    suspend fun addToCart(
        @Body request: CartItemRequest
    ): ApiResponse<CartDto>

    @PATCH("api/cart/items/{productId}")
    suspend fun updateCartItemQuantity(
        @Path("productId") productId: Long,
        @Query("quantity") quantity: Int
    ): ApiResponse<CartDto>

    @DELETE("api/cart/items/{productId}")
    suspend fun removeCartItem(
        @Path("productId") productId: Long
    ): ApiResponse<CartDto>

    @DELETE("api/cart")
    suspend fun clearCart(): ApiResponse<Unit>

    // ==========================================
    // 4. YÊU THÍCH (FAVOURITES)
    // ==========================================

    @GET("api/favourites")
    suspend fun getFavourites(): ApiResponse<List<FavouriteDto>>

    @POST("api/favourites/toggle")
    suspend fun toggleFavourite(
        @Query("productId") productId: Long
    ): ApiResponse<Boolean>

    @GET("api/favourites/check")
    suspend fun checkFavourite(
        @Query("productId") productId: Long
    ): ApiResponse<Boolean>

    @GET("api/favourites/count/{productId}")
    suspend fun getFavouriteCount(
        @Path("productId") productId: Long
    ): ApiResponse<Long>

    @DELETE("api/favourites")
    suspend fun removeFavourite(
        @Query("productId") productId: Long
    ): ApiResponse<Unit>

    // ==========================================
    // 5. COUPON
    // ==========================================

    @GET("api/coupons/active")
    suspend fun getActiveCoupons(): ApiResponse<List<CouponDto>>

    @GET("api/coupons/paged")
    suspend fun getCouponsPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<CouponDto>>

    @GET("api/coupons/{id}")
    suspend fun getCouponById(
        @Path("id") id: Long
    ): ApiResponse<CouponDto>

    @POST("api/coupons/apply")
    suspend fun applyCoupon(
        @Body request: CouponApplyRequest
    ): ApiResponse<Double>

    @POST("api/coupons")
    suspend fun createCoupon(
        @Body coupon: CouponDto
    ): ApiResponse<CouponDto>

    @PUT("api/coupons/{id}")
    suspend fun updateCoupon(
        @Path("id") id: Long,
        @Body coupon: CouponDto
    ): ApiResponse<CouponDto>

    @DELETE("api/coupons/{id}")
    suspend fun deleteCoupon(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    // ==========================================
    // 6. ĐƠN HÀNG (ORDERS)
    // ==========================================

    @POST("api/orders/checkout-cart")
    suspend fun checkoutFromCart(
        @Body request: CheckoutRequest
    ): ApiResponse<OrderDto>

    @GET("api/orders/me")
    suspend fun getMyOrders(): ApiResponse<List<OrderDto>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: Long
    ): ApiResponse<OrderDto>

    @GET("api/orders/code/{orderCode}")
    suspend fun getOrderByCode(
        @Path("orderCode") orderCode: String
    ): ApiResponse<OrderDto>

    @GET("api/orders/{id}/tracking")
    suspend fun getOrderTracking(
        @Path("id") id: Long
    ): ApiResponse<List<OrderTrackingDto>>

    @PATCH("api/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: Long
    ): ApiResponse<OrderDto>

    // Admin Đơn hàng
    @GET("api/orders/paged")
    suspend fun getOrdersPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<OrderDto>>

    @GET("api/orders/search")
    suspend fun searchOrders(
        @Query("statusId") statusId: Long? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<OrderDto>>

    @PATCH("api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Query("statusId") statusId: Long
    ): ApiResponse<OrderDto>

    @PATCH("api/orders/{id}/payment-status")
    suspend fun updateOrderPaymentStatus(
        @Path("id") id: Long,
        @Query("paymentStatus") paymentStatus: String // UNPAID | PAID
    ): ApiResponse<OrderDto>

    @DELETE("api/orders/{id}")
    suspend fun deleteOrder(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    @GET("api/order-statuses")
    suspend fun getOrderStatuses(): ApiResponse<List<OrderStatusDto>>

    // ==========================================
    // 7. HỒ SƠ & NGƯỜI DÙNG (USERS)
    // ==========================================

    @GET("api/users/me")
    suspend fun getMyProfile(): ApiResponse<UserDto>

    @PUT("api/users/me")
    suspend fun updateMyProfile(
        @Body profile: UserDto
    ): ApiResponse<UserDto>

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadMyAvatar(
        @Part file: MultipartBody.Part
    ): ApiResponse<UserDto>

    // Admin Users
    @GET("api/users/paged")
    suspend fun getUsersPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<UserDto>>

    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<PageResponse<UserDto>>

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long
    ): ApiResponse<UserDto>

    @POST("api/users")
    suspend fun createUser(
        @Body request: RegisterRequest
    ): ApiResponse<UserDto>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body dto: UserDto
    ): ApiResponse<UserDto>

    @PATCH("api/users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: Long,
        @Query("status") status: Boolean
    ): ApiResponse<UserDto>

    @Multipart
    @POST("api/users/{id}/avatar")
    suspend fun uploadUserAvatar(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): ApiResponse<UserDto>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Long
    ): ApiResponse<Unit>

    // ==========================================
    // 8. DASHBOARD ADMIN
    // ==========================================

    @GET("api/admin/dashboard")
    suspend fun getDashboardStats(): ApiResponse<DashboardDto>

    // ==========================================
    // 9. KHO / LÔ HÀNG (BATCHES & INVENTORY)
    // ==========================================

    @POST("api/admin/batches")
    suspend fun receiveBatch(
        @Body request: BatchRequest
    ): ApiResponse<BatchDto>

    @GET("api/admin/batches/product/{productId}")
    suspend fun getBatchesByProduct(
        @Path("productId") productId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<BatchDto>>

    @GET("api/admin/batches/expiring")
    suspend fun getExpiringBatches(
        @Query("days") days: Int = 2,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<BatchDto>>

    @GET("api/admin/batches/waste-report")
    suspend fun getWasteReport(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): ApiResponse<WasteReportDto>

    @POST("api/admin/batches/write-off-expired")
    suspend fun writeOffExpiredBatches(): ApiResponse<Int>
}

