package com.hieu.ui.admin.coupon

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.hieu.core.theme.PrimaryGreen
import com.hieu.core.util.UiState
import com.hieu.core.util.formatVnd
import com.hieu.data.remote.dto.CouponDto
import com.hieu.ui.components.EmptyContent
import com.hieu.ui.components.ErrorRetryView
import com.hieu.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponsScreen(
    onNavigateToAddCoupon: () -> Unit,
    onNavigateToEditCoupon: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: AdminCouponsViewModel = hiltViewModel()
) {
    val couponsState by viewModel.couponsState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    var couponToDelete by remember { mutableStateOf<CouponDto?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (couponToDelete != null) {
        AlertDialog(
            onDismissRequest = { couponToDelete = null },
            title = { Text("Xác nhận xóa mã giảm giá") },
            text = { Text("Bạn có chắc muốn xóa mã '${couponToDelete?.code}' không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        couponToDelete?.id?.let { viewModel.deleteCoupon(it) }
                        couponToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { couponToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý mã giảm giá", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onOpenDrawer) {
                        Text("Menu", fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddCoupon) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm mã giảm giá")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCoupon,
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mã giảm giá")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = couponsState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorRetryView(
                    message = state.message,
                    onRetry = { viewModel.loadCoupons() }
                )
                is UiState.Empty -> EmptyContent(
                    message = "Chưa có mã giảm giá nào",
                    actionLabel = "Tạo mã mới",
                    onAction = onNavigateToAddCoupon
                )
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data, key = { it.id ?: 0L }) { coupon ->
                            AdminCouponItem(
                                coupon = coupon,
                                onEdit = { coupon.id?.let { onNavigateToEditCoupon(it) } },
                                onDelete = { couponToDelete = coupon }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCouponItem(
    coupon: CouponDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = coupon.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (coupon.active == true) PrimaryGreen.copy(alpha = 0.15f)
                                else Color.LightGray.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (coupon.active == true) "Hoạt động" else "Khóa",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (coupon.active == true) PrimaryGreen else Color.DarkGray
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryGreen)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            coupon.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            coupon.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val discountLabel = if (coupon.discountType == "PERCENT") {
                "Giảm ${coupon.discountValue}%" + (coupon.maxDiscount?.let { " (tối đa ${formatVnd(it)})" } ?: "")
            } else {
                "Giảm ${formatVnd(coupon.discountValue)}"
            }

            Text(
                text = discountLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            coupon.minOrderValue?.let {
                Text(
                    text = "Đơn tối thiểu: ${formatVnd(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            val usageText = "Đã dùng: ${coupon.usedCount ?: 0}" +
                    (coupon.usageLimit?.let { "/$it lượt" } ?: " (Không giới hạn)")
            Text(
                text = usageText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (!coupon.startDate.isNullOrBlank() || !coupon.endDate.isNullOrBlank()) {
                Text(
                    text = "Thời hạn: ${coupon.startDate ?: "..."} đến ${coupon.endDate ?: "..."}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

