package com.hieu.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.core.theme.PrimaryGreen
import com.hieu.ui.admin.batch.AdminBatchesScreen
import com.hieu.ui.admin.category.AdminCategoriesScreen
import com.hieu.ui.admin.coupon.AdminCouponsScreen
import com.hieu.ui.admin.dashboard.AdminDashboardScreen
import com.hieu.ui.admin.order.AdminOrdersScreen
import com.hieu.ui.admin.product.AdminProductsScreen
import com.hieu.ui.admin.user.AdminUsersScreen
import kotlinx.coroutines.launch

enum class AdminDrawerTab(val title: String) {
    DASHBOARD("Tổng quan"),
    PRODUCTS("Sản phẩm"),
    CATEGORIES("Danh mục"),
    COUPONS("Mã giảm giá"),
    ORDERS("Đơn hàng"),
    BATCHES("Kho & Lô hàng"),
    USERS("Người dùng")
}

@Composable
fun AdminMainScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Long) -> Unit,
    onNavigateToAddCoupon: () -> Unit,
    onNavigateToEditCoupon: (Long) -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onNavigateToReceiveBatch: () -> Unit,
    onNavigateToWasteReport: () -> Unit,
    onNavigateToAddUser: () -> Unit,
    onNavigateToEditUser: (Long) -> Unit,
    onNavigateToStore: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: AdminMainViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(AdminDrawerTab.DASHBOARD) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val session by viewModel.sessionState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logoutSuccess.collect {
            onLogoutSuccess()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất quản trị") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryGreen)
                            .padding(20.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (session.username?.take(1) ?: "A").uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "IND MART ADMIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = session.username ?: "Quản trị viên",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Menu items
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Tổng quan (Dashboard)") },
                        selected = currentTab == AdminDrawerTab.DASHBOARD,
                        onClick = {
                            currentTab = AdminDrawerTab.DASHBOARD
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                        label = { Text("Quản lý sản phẩm") },
                        selected = currentTab == AdminDrawerTab.PRODUCTS,
                        onClick = {
                            currentTab = AdminDrawerTab.PRODUCTS
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Category, contentDescription = null) },
                        label = { Text("Quản lý danh mục") },
                        selected = currentTab == AdminDrawerTab.CATEGORIES,
                        onClick = {
                            currentTab = AdminDrawerTab.CATEGORIES
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Discount, contentDescription = null) },
                        label = { Text("Mã giảm giá (Coupon)") },
                        selected = currentTab == AdminDrawerTab.COUPONS,
                        onClick = {
                            currentTab = AdminDrawerTab.COUPONS
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text("Quản lý đơn hàng") },
                        selected = currentTab == AdminDrawerTab.ORDERS,
                        onClick = {
                            currentTab = AdminDrawerTab.ORDERS
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        label = { Text("Kho & Lô hàng") },
                        selected = currentTab == AdminDrawerTab.BATCHES,
                        onClick = {
                            currentTab = AdminDrawerTab.BATCHES
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Quản lý người dùng") },
                        selected = currentTab == AdminDrawerTab.USERS,
                        onClick = {
                            currentTab = AdminDrawerTab.USERS
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.weight(1f, fill = false))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        label = { Text("Xem gian hàng") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onNavigateToStore()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        label = { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                showLogoutDialog = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        when (currentTab) {
            AdminDrawerTab.DASHBOARD -> AdminDashboardScreen()
            AdminDrawerTab.PRODUCTS -> AdminProductsScreen(
                onAddProductClick = onNavigateToAddProduct,
                onEditProductClick = onNavigateToEditProduct
            )
            AdminDrawerTab.CATEGORIES -> AdminCategoriesScreen(
                onAddCategoryClick = onNavigateToAddCategory,
                onEditCategoryClick = onNavigateToEditCategory
            )
            AdminDrawerTab.COUPONS -> AdminCouponsScreen(
                onNavigateToAddCoupon = onNavigateToAddCoupon,
                onNavigateToEditCoupon = onNavigateToEditCoupon,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            AdminDrawerTab.ORDERS -> AdminOrdersScreen(
                onNavigateToOrderDetail = onNavigateToOrderDetail,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            AdminDrawerTab.BATCHES -> AdminBatchesScreen(
                onNavigateToReceiveBatch = onNavigateToReceiveBatch,
                onNavigateToWasteReport = onNavigateToWasteReport,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            AdminDrawerTab.USERS -> AdminUsersScreen(
                onNavigateToAddUser = onNavigateToAddUser,
                onNavigateToEditUser = onNavigateToEditUser,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
    }
}
