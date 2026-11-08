package com.hieu.ui.navigation

sealed class Screen(val route: String) {
    // Khởi động
    data object Splash : Screen("splash")

    // Xác thực
    data object Login : Screen("login")
    data object RegisterInfo : Screen("register_info")
    data object ForgotPasswordInfo : Screen("forgot_password_info")

    // Khách hàng / Người dùng
    data object UserMain : Screen("user_main")
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    data object CategoryProducts : Screen("category_products/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: Long, categoryName: String) = "category_products/$categoryId/$categoryName"
    }
    data object Search : Screen("search?keyword={keyword}&categoryId={categoryId}") {
        fun createRoute(keyword: String = "", categoryId: Long? = null): String {
            val catParam = if (categoryId != null) "&categoryId=$categoryId" else ""
            return "search?keyword=$keyword$catParam"
        }
    }
    data object Checkout : Screen("checkout")
    data object OrderSuccess : Screen("order_success/{orderCode}") {
        fun createRoute(orderCode: String) = "order_success/$orderCode"
    }
    data object MyOrders : Screen("my_orders")
    data object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
    data object Favourites : Screen("favourites")
    data object ActiveCoupons : Screen("active_coupons")
    data object ProfileEdit : Screen("profile_edit")

    // Quản trị viên (Admin)
    data object AdminMain : Screen("admin_main")
    data object AdminProductEdit : Screen("admin_product_edit/{productId}") {
        fun createRoute(productId: Long = 0L) = "admin_product_edit/$productId"
    }
    data object AdminCategoryEdit : Screen("admin_category_edit/{categoryId}") {
        fun createRoute(categoryId: Long = 0L) = "admin_category_edit/$categoryId"
    }
    data object AdminCouponEdit : Screen("admin_coupon_edit/{couponId}") {
        fun createRoute(couponId: Long = 0L) = "admin_coupon_edit/$couponId"
    }
    data object AdminOrderDetail : Screen("admin_order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "admin_order_detail/$orderId"
    }
    data object AdminBatchReceive : Screen("admin_batch_receive?productId={productId}") {
        fun createRoute(productId: Long? = null) = if (productId != null) "admin_batch_receive?productId=$productId" else "admin_batch_receive"
    }
    data object AdminWasteReport : Screen("admin_waste_report")
    data object AdminUserEdit : Screen("admin_user_edit/{userId}") {
        fun createRoute(userId: Long = 0L) = "admin_user_edit/$userId"
    }
}

// Các tab chính trong Bottom Navigation của Người dùng
sealed class UserBottomTab(val route: String, val title: String) {
    data object Home : UserBottomTab("tab_home", "Trang chủ")
    data object Categories : UserBottomTab("tab_categories", "Danh mục")
    data object Cart : UserBottomTab("tab_cart", "Giỏ hàng")
    data object Profile : UserBottomTab("tab_profile", "Cá nhân")
}

// Các mục quản lý trong Admin Drawer / Rail
sealed class AdminDrawerItem(val route: String, val title: String) {
    data object Dashboard : AdminDrawerItem("admin_dashboard", "Tổng quan")
    data object Products : AdminDrawerItem("admin_products", "Sản phẩm")
    data object Categories : AdminDrawerItem("admin_categories", "Danh mục")
    data object Coupons : AdminDrawerItem("admin_coupons", "Mã giảm giá")
    data object Orders : AdminDrawerItem("admin_orders", "Đơn hàng")
    data object Inventory : AdminDrawerItem("admin_inventory", "Kho & Lô hàng")
    data object Users : AdminDrawerItem("admin_users", "Người dùng")
}

