package com.hieu.ui.admin.order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hieu.core.theme.PrimaryGreen
import com.hieu.core.util.formatVnd
import com.hieu.data.remote.dto.OrderDto
import com.hieu.ui.components.EmptyContent
import com.hieu.ui.components.ErrorRetryView
import com.hieu.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onNavigateToOrderDetail: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: AdminOrdersViewModel = hiltViewModel()
) {
    val statuses by viewModel.statuses.collectAsState()
    val selectedStatusId by viewModel.selectedStatusId.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val orders = viewModel.ordersStream.collectAsLazyPagingItems()
    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onOpenDrawer) {
                        Text("Menu", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { viewModel.onKeywordChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm theo mã đơn, tên khách, SĐT...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchKeyword.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onKeywordChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusId == null,
                    onClick = { viewModel.onStatusSelected(null) },
                    label = { Text("Tất cả") }
                )
                statuses.forEach { status ->
                    FilterChip(
                        selected = selectedStatusId == status.id,
                        onClick = { viewModel.onStatusSelected(status.id) },
                        label = { Text(status.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Orders List
            Box(modifier = Modifier.fillMaxSize()) {
                if (orders.loadState.refresh is LoadState.Loading) {
                    LoadingView()
                } else if (orders.loadState.refresh is LoadState.Error) {
                    val error = orders.loadState.refresh as LoadState.Error
                    ErrorRetryView(
                        message = error.error.localizedMessage ?: "Lỗi tải danh sách đơn hàng",
                        onRetry = { orders.retry() }
                    )
                } else if (orders.itemCount == 0) {
                    EmptyContent(
                        message = "Không tìm thấy đơn hàng nào",
                        actionLabel = "Tải lại",
                        onAction = { orders.refresh() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders.itemCount) { index ->
                            orders[index]?.let { order ->
                                AdminOrderCard(
                                    order = order,
                                    onClick = { order.id?.let { onNavigateToOrderDetail(it) } }
                                )
                            }
                        }

                        if (orders.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
fun AdminOrderCard(
    order: OrderDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn #${order.orderCode ?: order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Status Badge
                val statusText = order.status ?: "Đang xử lý"
                val (badgeBg, badgeColor) = when {
                    statusText.contains("giao", ignoreCase = true) || statusText.contains("hoàn thành", ignoreCase = true) ->
                        Pair(PrimaryGreen.copy(alpha = 0.15f), PrimaryGreen)
                    statusText.contains("hủy", ignoreCase = true) ->
                        Pair(Color.Red.copy(alpha = 0.15f), Color.Red)
                    else ->
                        Pair(Color(0xFF1976D2).copy(alpha = 0.15f), Color(0xFF1976D2))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor
                    )
                }
            }

            Text(
                text = "Khách hàng: ${order.customerName} - ${order.phone}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (order.address.isNotBlank()) {
                Text(
                    text = "Địa chỉ: ${order.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.items.size} mặt hàng",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    order.paymentMethod?.let {
                        Text(
                            text = "TT: $it (${if (order.paymentStatus == "PAID") "Đã thanh toán" else "Chưa thanh toán"})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (order.paymentStatus == "PAID") PrimaryGreen else Color(0xFFE65100)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatVnd(order.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    order.orderDate?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

