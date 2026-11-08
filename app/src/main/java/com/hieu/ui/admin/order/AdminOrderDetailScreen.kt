package com.hieu.ui.admin.order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.BuildConfig
import com.hieu.core.theme.PrimaryGreen
import com.hieu.core.util.UiState
import com.hieu.core.util.formatVnd
import com.hieu.data.remote.dto.OrderItemDto
import com.hieu.data.remote.dto.OrderStatusDto
import com.hieu.data.remote.dto.OrderTrackingDto
import com.hieu.ui.components.ErrorRetryView
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminOrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    var showStatusDialog by remember { mutableStateOf(false) }
    var showPaymentStatusDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.orderDeleted.collect { deleted ->
            if (deleted) {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn #${viewModel.orderId}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa đơn", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorRetryView(
                    message = state.message,
                    onRetry = { viewModel.loadOrderDetail() }
                )
                is UiState.Empty -> ErrorRetryView(
                    message = "Không tìm thấy thông tin đơn hàng",
                    onRetry = { viewModel.loadOrderDetail() }
                )
                is UiState.Success -> {
                    val order = state.data.order
                    val tracking = state.data.tracking
                    val statuses = state.data.statuses

                    if (order == null) {
                        ErrorRetryView(
                            message = "Không tìm thấy thông tin đơn hàng",
                            onRetry = { viewModel.loadOrderDetail() }
                        )
                        return@Box
                    }

                    // Dialog Đổi Trạng Thái Đơn Hàng
                    if (showStatusDialog) {
                        StatusSelectionDialog(
                            statuses = statuses,
                            currentStatusId = order.statusId,
                            onDismiss = { showStatusDialog = false },
                            onConfirm = { newStatusId ->
                                viewModel.updateStatus(newStatusId)
                                showStatusDialog = false
                            }
                        )
                    }

                    // Dialog Đổi Trạng Thái Thanh Toán
                    if (showPaymentStatusDialog) {
                        PaymentStatusSelectionDialog(
                            currentPaymentStatus = order.paymentStatus ?: "UNPAID",
                            onDismiss = { showPaymentStatusDialog = false },
                            onConfirm = { newPaymentStatus ->
                                viewModel.updatePaymentStatus(newPaymentStatus)
                                showPaymentStatusDialog = false
                            }
                        )
                    }

                    // Dialog Xác Nhận Xóa Đơn Hàng
                    if (showDeleteConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmDialog = false },
                            title = { Text("Xác nhận xóa đơn hàng") },
                            text = { Text("Bạn có chắc chắn muốn xóa đơn hàng #${order.orderCode ?: order.id}? Hành động này không thể hoàn tác.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteConfirmDialog = false
                                        viewModel.deleteOrder()
                                    }
                                ) {
                                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                    Text("Hủy")
                                }
                            }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Thẻ Trạng Thái & Thao Tác
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Trạng thái giao hàng:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(
                                            text = order.status ?: "Đang xử lý",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showStatusDialog = true },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Đổi trạng thái")
                                    }
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Thanh toán: ${order.paymentMethod ?: "COD"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(
                                            text = if (order.paymentStatus == "PAID") "ĐÃ THANH TOÁN" else "CHƯA THANH TOÁN",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (order.paymentStatus == "PAID") PrimaryGreen else Color(0xFFE65100)
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showPaymentStatusDialog = true },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Cập nhật")
                                    }
                                }
                            }
                        }

                        // Thông Tin Khách Hàng
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Thông tin người nhận", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Khách hàng: ${order.customerName}", style = MaterialTheme.typography.bodyMedium)
                                Text("Số điện thoại: ${order.phone}", style = MaterialTheme.typography.bodyMedium)
                                order.email?.takeIf { it.isNotBlank() }?.let {
                                    Text("Email: $it", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("Địa chỉ: ${order.address}", style = MaterialTheme.typography.bodyMedium)
                                order.note?.takeIf { it.isNotBlank() }?.let {
                                    Text("Ghi chú: $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }

                        // Danh Sách Sản Phẩm
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Danh sách sản phẩm (${order.items.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                order.items.forEach { item ->
                                    AdminOrderItemRow(item = item)
                                    HorizontalDivider()
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mã giảm giá đã dùng:")
                                    Text(order.couponCode ?: "Không có", fontWeight = FontWeight.SemiBold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng thanh toán:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = formatVnd(order.total),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Lịch Sử Theo Dõi (Tracking)
                        if (tracking.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Lịch sử đơn hàng", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                    tracking.forEach { track ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryGreen)
                                            )
                                            Column {
                                                Text(
                                                    text = track.status ?: "Cập nhật",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                track.note?.let {
                                                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                                track.time?.let {
                                                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrderItemRow(item: OrderItemDto) {
    val imageUrl = item.productImage?.let {
        if (it.startsWith("http")) it else "${BuildConfig.BASE_URL.removeSuffix("/")}/$it"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = item.productName,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = "${formatVnd(item.price)} x ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            text = formatVnd(item.subtotal),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusSelectionDialog(
    statuses: List<OrderStatusDto>,
    currentStatusId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedId by remember { mutableStateOf(currentStatusId ?: statuses.firstOrNull()?.id ?: 1L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn trạng thái mới") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedId == status.id,
                            onClick = { selectedId = status.id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(status.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            status.description?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedId) }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun PaymentStatusSelectionDialog(
    currentPaymentStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentPaymentStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Trạng thái thanh toán") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedStatus == "UNPAID",
                        onClick = { selectedStatus = "UNPAID" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chưa thanh toán (UNPAID)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedStatus == "PAID",
                        onClick = { selectedStatus = "PAID" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đã thanh toán (PAID)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedStatus) }) {
                Text("Cập nhật")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

