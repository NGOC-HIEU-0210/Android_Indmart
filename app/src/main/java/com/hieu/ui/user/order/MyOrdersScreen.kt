package com.hieu.ui.user.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.OrderDto
import com.hieu.ui.components.IndMartEmptyState
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onNavigateBack: () -> Unit,
    onOrderClick: (Long) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = ordersState) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải danh sách đơn hàng...")
                }
                is UiState.Empty -> {
                    IndMartEmptyState(
                        message = "Bạn chưa có đơn hàng nào tại IND MART.",
                        actionText = "Khám phá sản phẩm",
                        onActionClick = onNavigateBack
                    )
                }
                is UiState.Error -> {
                    IndMartErrorMessage(
                        message = state.message,
                        onRetry = viewModel::loadMyOrders
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data, key = { it.id ?: 0L }) { order ->
                            OrderCardItem(
                                order = order,
                                onClick = { order.id?.let(onOrderClick) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCardItem(
    order: OrderDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã đơn: ${order.orderCode ?: "#${order.id}"}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (!order.orderDate.isNullOrBlank()) {
                Text(
                    text = "Ngày đặt: ${Formatters.formatDateTime(order.orderDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Số lượng sản phẩm: ${order.items.size} món",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phương thức: ${if (order.paymentMethod == "BANK_TRANSFER") "Chuyển khoản" else "COD"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = Formatters.formatVnd(order.total),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: String?, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status?.uppercase()) {
        "PENDING" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Chờ xác nhận")
        "CONFIRMED" -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "Đã xác nhận")
        "SHIPPING" -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "Đang giao")
        "COMPLETED" -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, "Hoàn thành")
        "CANCELLED" -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "Đã hủy")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, status ?: "Không rõ")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

