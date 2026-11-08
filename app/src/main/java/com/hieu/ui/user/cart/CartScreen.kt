package com.hieu.ui.user.cart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CartDto
import com.hieu.data.remote.dto.CartItemDto
import com.hieu.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckoutClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCartChanged: () -> Unit,
    onProductClick: (Long) -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
            onCartChanged()
        }
    }

    if (!viewModel.isLoggedIn()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Giỏ hàng") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { paddingValues ->
            IndMartEmptyState(
                message = "Vui lòng đăng nhập để xem và quản lý giỏ hàng của bạn.",
                actionText = "Đăng nhập ngay",
                onActionClick = onLoginClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
        return
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Xác nhận xóa giỏ hàng") },
            text = { Text("Bạn có chắc chắn muốn xóa toàn bộ sản phẩm khỏi giỏ hàng?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearCart()
                    }
                ) {
                    Text("Xóa hết", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng") },
                actions = {
                    if (state.cartState is UiState.Success) {
                        TextButton(onClick = { showClearConfirmDialog = true }) {
                            Text("Xóa hết", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (state.cartState is UiState.Success) {
                val cart = (state.cartState as UiState.Success<CartDto>).data
                val finalTotal = maxOf(0.0, cart.subtotal - state.discountAmount)
                val hasUnavailableItems = cart.items.any { it.available == false }

                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tóm tắt tiền
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tạm tính:", style = MaterialTheme.typography.bodyMedium)
                            Text(Formatters.formatVnd(cart.subtotal), style = MaterialTheme.typography.bodyMedium)
                        }

                        if (state.discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Giảm giá:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Text("-${Formatters.formatVnd(state.discountAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng cộng:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                Formatters.formatVnd(finalTotal),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (hasUnavailableItems) {
                            Text(
                                text = "Vui lòng xóa các món không khả dụng trước khi đặt hàng",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IndMartButton(
                            text = "Tiến hành đặt hàng",
                            onClick = onCheckoutClick,
                            enabled = !hasUnavailableItems && cart.items.isNotEmpty()
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val cartState = state.cartState) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải giỏ hàng...")
                }
                is UiState.Empty -> {
                    IndMartEmptyState(
                        message = "Giỏ hàng của bạn đang trống.",
                        actionText = "Mua sắm ngay",
                        onActionClick = { /* User can navigate back to home */ }
                    )
                }
                is UiState.Error -> {
                    IndMartErrorMessage(
                        message = cartState.message,
                        onRetry = viewModel::loadCart
                    )
                }
                is UiState.Success -> {
                    val cart = cartState.data

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Danh sách món trong giỏ
                        items(cart.items, key = { it.productId }) { item ->
                            CartItemRow(
                                item = item,
                                onIncrement = { viewModel.updateQuantity(item.productId, item.quantity, 1) },
                                onDecrement = { viewModel.updateQuantity(item.productId, item.quantity, -1) },
                                onRemove = { viewModel.removeItem(item.productId) },
                                onClick = { onProductClick(item.productId) }
                            )
                        }

                        // Mục áp dụng mã giảm giá
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Mã giảm giá (Coupon)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = state.couponCode,
                                            onValueChange = viewModel::onCouponCodeChange,
                                            placeholder = { Text("Nhập mã") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                                focusedContainerColor = MaterialTheme.colorScheme.surface
                                            )
                                        )

                                        Button(
                                            onClick = viewModel::applyCoupon,
                                            enabled = !state.isApplyingCoupon && state.couponCode.isNotBlank(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            if (state.isApplyingCoupon) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            } else {
                                                Text("Áp dụng")
                                            }
                                        }
                                    }

                                    if (!state.couponError.isNullOrBlank()) {
                                        Text(
                                            text = state.couponError!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    if (!state.couponSuccessMessage.isNullOrBlank()) {
                                        Text(
                                            text = state.couponSuccessMessage!!,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemDto,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Cảnh báo không khả dụng
            if (item.available == false) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Sản phẩm này tạm thời không khả dụng!",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ảnh
                AsyncImage(
                    model = Formatters.toFullImageUrl(item.productImage),
                    contentDescription = item.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onClick)
                )

                // Chi tiết tên & giá
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = Formatters.formatVnd(item.price),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        if ((item.originalPrice ?: 0.0) > item.price) {
                            Text(
                                text = Formatters.formatVnd(item.originalPrice),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Nút xóa
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa món",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bộ tăng giảm số lượng & Thành tiền
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                ) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                        Text("-", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                        Text("+", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Text(
                    text = "Tổng: ${Formatters.formatVnd(item.subtotal)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

