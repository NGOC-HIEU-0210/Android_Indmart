package com.hieu.ui.user.order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.OrderDto
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Xác nhận hủy đơn hàng") },
            text = { Text("Bạn có chắc chắn muốn hủy đơn hàng này? Thao tác này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                        viewModel.cancelOrder()
                    }
                ) {
                    Text("Xác nhận hủy", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng") },
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
            when (val orderState = state.orderState) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải thông tin đơn hàng...")
                }
                is UiState.Error -> {
                    IndMartErrorMessage(
                        message = orderState.message,
                        onRetry = viewModel::loadDetail
                    )
                }
                is UiState.Success -> {
                    val order = orderState.data
                    // Khách huỷ đơn của mình (chỉ khi đơn đang PENDING và chưa thanh toán)
                    val isPending = order.status?.equals("PENDING", ignoreCase = true) == true
                    val isUnpaid = order.paymentStatus?.equals("UNPAID", ignoreCase = true) != false
                    val canCancel = isPending && isUnpaid

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Thẻ tóm tắt đơn
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mã đơn: ${order.orderCode ?: "#${order.id}"}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    OrderStatusBadge(status = order.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (!order.orderDate.isNullOrBlank()) {
                                    Text(
                                        text = "Thời gian đặt: ${Formatters.formatDateTime(order.orderDate)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "Trạng thái thanh toán: ${if (order.paymentStatus == "PAID") "Đã thanh toán" else "Chưa thanh toán"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (order.paymentStatus == "PAID") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Danh sách sản phẩm trong đơn
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Sản phẩm (${order.items.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.productName,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                            )
                                            Text(
                                                text = "${item.quantity} x ${Formatters.formatVnd(item.price)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = Formatters.formatVnd(item.subtotal),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng thanh toán:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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

                        // Thông tin nhận hàng
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Địa chỉ nhận hàng",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("Người nhận: ${order.customerName}", style = MaterialTheme.typography.bodyMedium)
                                Text("Số điện thoại: ${order.phone}", style = MaterialTheme.typography.bodyMedium)
                                Text("Địa chỉ: ${order.address}", style = MaterialTheme.typography.bodyMedium)
                                if (!order.note.isNullOrBlank()) {
                                    Text("Ghi chú: ${order.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Lịch sử trạng thái đơn hàng (Tracking timeline)
                        if (state.trackingList.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Tiến trình đơn hàng",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    state.trackingList.forEachIndexed { index, track ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                                if (index < state.trackingList.size - 1) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(36.dp)
                                                            .background(MaterialTheme.colorScheme.outlineVariant)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                                Text(
                                                    text = track.status ?: "Cập nhật",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                if (!track.note.isNullOrBlank()) {
                                                    Text(
                                                        text = track.note,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (!track.time.isNullOrBlank()) {
                                                    Text(
                                                        text = Formatters.formatDateTime(track.time),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Nút hủy đơn (nếu đủ điều kiện PENDING và UNPAID)
                        if (canCancel) {
                            OutlinedButton(
                                onClick = { showCancelConfirmDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                if (state.isCancelling) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Hủy đơn hàng này")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                UiState.Empty -> Unit
            }
        }
    }
}

