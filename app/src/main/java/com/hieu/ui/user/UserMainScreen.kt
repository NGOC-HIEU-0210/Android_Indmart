package com.hieu.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.data.local.TokenManager
import com.hieu.data.local.UserSession
import com.hieu.data.repository.CartRepository
import com.hieu.ui.navigation.UserBottomTab
import com.hieu.ui.user.cart.CartScreen
import com.hieu.ui.user.category.CategoriesScreen
import com.hieu.ui.user.home.HomeScreen
import com.hieu.ui.user.profile.ProfileScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserMainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val cartRepository: CartRepository
) : ViewModel() {

    val session: StateFlow<UserSession> = tokenManager.sessionState

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    init {
        refreshCartCount()
    }

    fun refreshCartCount() {
        if (!tokenManager.isLoggedIn()) {
            _cartCount.value = 0
            return
        }
        viewModelScope.launch {
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    _cartCount.value = result.data.totalQuantity
                }
                else -> Unit
            }
        }
    }
}

@Composable
fun UserMainScreen(
    onNavigateToProductDetail: (Long) -> Unit,
    onNavigateToCategoryProducts: (Long, String) -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToFavourites: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: UserMainViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf<UserBottomTab>(UserBottomTab.Home) }
    val cartCount by viewModel.cartCount.collectAsState()
    val session by viewModel.session.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshCartCount()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                // Tab Trang chủ
                NavigationBarItem(
                    selected = selectedTab == UserBottomTab.Home,
                    onClick = { selectedTab = UserBottomTab.Home },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") }
                )

                // Tab Danh mục
                NavigationBarItem(
                    selected = selectedTab == UserBottomTab.Categories,
                    onClick = { selectedTab = UserBottomTab.Categories },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Danh mục") },
                    label = { Text("Danh mục") }
                )

                // Tab Giỏ hàng
                NavigationBarItem(
                    selected = selectedTab == UserBottomTab.Cart,
                    onClick = {
                        selectedTab = UserBottomTab.Cart
                        viewModel.refreshCartCount()
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge { Text("$cartCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng")
                        }
                    },
                    label = { Text("Giỏ hàng") }
                )

                // Tab Cá nhân
                NavigationBarItem(
                    selected = selectedTab == UserBottomTab.Profile,
                    onClick = { selectedTab = UserBottomTab.Profile },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
                    label = { Text("Cá nhân") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                UserBottomTab.Home -> HomeScreen(
                    onProductClick = onNavigateToProductDetail,
                    onCategoryClick = onNavigateToCategoryProducts,
                    onCartUpdated = { viewModel.refreshCartCount() }
                )
                UserBottomTab.Categories -> CategoriesScreen(
                    onProductClick = onNavigateToProductDetail,
                    onCategoryClick = onNavigateToCategoryProducts
                )
                UserBottomTab.Cart -> CartScreen(
                    onCheckoutClick = onNavigateToCheckout,
                    onLoginClick = onNavigateToLogin,
                    onCartChanged = { viewModel.refreshCartCount() },
                    onProductClick = onNavigateToProductDetail
                )
                UserBottomTab.Profile -> ProfileScreen(
                    onOrdersClick = onNavigateToOrders,
                    onFavouritesClick = onNavigateToFavourites,
                    onCouponsClick = onNavigateToCoupons,
                    onEditProfileClick = onNavigateToProfileEdit,
                    onLoginClick = onNavigateToLogin,
                    onAdminPortalClick = onNavigateToAdmin
                )
            }
        }
    }
}

