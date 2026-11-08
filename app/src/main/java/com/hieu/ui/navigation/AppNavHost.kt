package com.hieu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hieu.data.local.TokenManager
import com.hieu.ui.admin.AdminMainScreen
import com.hieu.ui.admin.batch.AdminBatchReceiveScreen
import com.hieu.ui.admin.batch.AdminWasteReportScreen
import com.hieu.ui.admin.category.AdminCategoryEditScreen
import com.hieu.ui.admin.coupon.AdminCouponEditScreen
import com.hieu.ui.admin.order.AdminOrderDetailScreen
import com.hieu.ui.admin.product.AdminProductEditScreen
import com.hieu.ui.admin.user.AdminUserEditScreen
import com.hieu.ui.auth.ForgotPasswordScreen
import com.hieu.ui.auth.LoginScreen
import com.hieu.ui.auth.RegisterScreen
import com.hieu.ui.splash.SplashScreen
import com.hieu.ui.user.UserMainScreen
import com.hieu.ui.user.checkout.CheckoutScreen
import com.hieu.ui.user.checkout.OrderSuccessScreen
import com.hieu.ui.user.coupon.ActiveCouponsScreen
import com.hieu.ui.user.favourite.FavouritesScreen
import com.hieu.ui.user.order.MyOrdersScreen
import com.hieu.ui.user.order.OrderDetailScreen
import com.hieu.ui.user.product.ProductDetailScreen
import com.hieu.ui.user.profile.ProfileEditScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    tokenManager: TokenManager,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route,
    onDeepLinkCallback: (() -> Unit)? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ==========================================
        // 1. MÀN HÌNH KHỞI ĐỘNG (SPLASH)
        // ==========================================
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminMain.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToUser = {
                    navController.navigate(Screen.UserMain.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 2. MÀN HÌNH XÁC THỰC (AUTH)
        // ==========================================
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { isAdmin ->
                    val destination = if (isAdmin) Screen.AdminMain.route else Screen.UserMain.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.RegisterInfo.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPasswordInfo.route)
                },
                onContinueAsGuest = {
                    navController.navigate(Screen.UserMain.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RegisterInfo.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPasswordInfo.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 3. LUỒNG NGƯỜI DÙNG / KHÁCH HÀNG (USER FLOW)
        // ==========================================
        composable(Screen.UserMain.route) {
            UserMainScreen(
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToCategoryProducts = { categoryId, categoryName ->
                    navController.navigate(Screen.ProductDetail.createRoute(categoryId))
                },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.MyOrders.route)
                },
                onNavigateToFavourites = {
                    navController.navigate(Screen.Favourites.route)
                },
                onNavigateToCoupons = {
                    navController.navigate(Screen.ActiveCoupons.route)
                },
                onNavigateToProfileEdit = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminMain.route)
                }
            )
        }

        // Chi tiết sản phẩm
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) {
            ProductDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onCartUpdated = { /* Tự động cập nhật qua CartRepository */ }
            )
        }

        // Đặt hàng (Checkout)
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onOrderPlacedSuccess = { orderCode ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderCode)) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                }
            )
        }

        // Đặt hàng thành công
        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(navArgument("orderCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderCode = backStackEntry.arguments?.getString("orderCode") ?: ""
            OrderSuccessScreen(
                orderCode = orderCode,
                onNavigateToOrders = {
                    navController.navigate(Screen.MyOrders.route) {
                        popUpTo(Screen.UserMain.route)
                    }
                },
                onContinueShopping = {
                    navController.navigate(Screen.UserMain.route) {
                        popUpTo(Screen.UserMain.route) { inclusive = true }
                    }
                }
            )
        }

        // Đơn hàng của tôi
        composable(Screen.MyOrders.route) {
            MyOrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }

        // Chi tiết & Tiến trình đơn hàng (User)
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) {
            OrderDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Yêu thích
        composable(Screen.Favourites.route) {
            FavouritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        // Mã giảm giá đang có
        composable(Screen.ActiveCoupons.route) {
            ActiveCouponsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Chỉnh sửa hồ sơ cá nhân
        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 4. LUỒNG QUẢN TRỊ VIÊN (ADMIN FLOW)
        // ==========================================
        composable(Screen.AdminMain.route) {
            AdminMainScreen(
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AdminProductEdit.createRoute(0L))
                },
                onNavigateToEditProduct = { productId ->
                    navController.navigate(Screen.AdminProductEdit.createRoute(productId))
                },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AdminCategoryEdit.createRoute(0L))
                },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(Screen.AdminCategoryEdit.createRoute(categoryId))
                },
                onNavigateToAddCoupon = {
                    navController.navigate(Screen.AdminCouponEdit.createRoute(0L))
                },
                onNavigateToEditCoupon = { couponId ->
                    navController.navigate(Screen.AdminCouponEdit.createRoute(couponId))
                },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Screen.AdminOrderDetail.createRoute(orderId))
                },
                onNavigateToReceiveBatch = {
                    navController.navigate(Screen.AdminBatchReceive.createRoute())
                },
                onNavigateToWasteReport = {
                    navController.navigate(Screen.AdminWasteReport.route)
                },
                onNavigateToAddUser = {
                    navController.navigate(Screen.AdminUserEdit.createRoute(0L))
                },
                onNavigateToEditUser = { userId ->
                    navController.navigate(Screen.AdminUserEdit.createRoute(userId))
                },
                onNavigateToStore = {
                    navController.navigate(Screen.UserMain.route)
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Thêm / Sửa sản phẩm Admin
        composable(
            route = Screen.AdminProductEdit.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            AdminProductEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Thêm / Sửa danh mục Admin
        composable(
            route = Screen.AdminCategoryEdit.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            AdminCategoryEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Thêm / Sửa mã giảm giá Admin
        composable(
            route = Screen.AdminCouponEdit.route,
            arguments = listOf(navArgument("couponId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            AdminCouponEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Chi tiết & Quản lý đơn hàng Admin
        composable(
            route = Screen.AdminOrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) {
            AdminOrderDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Nhập lô hàng mới
        composable(
            route = Screen.AdminBatchReceive.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            AdminBatchReceiveScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Báo cáo hao hụt & hủy hàng
        composable(Screen.AdminWasteReport.route) {
            AdminWasteReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Thêm / Sửa người dùng
        composable(
            route = Screen.AdminUserEdit.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            AdminUserEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
