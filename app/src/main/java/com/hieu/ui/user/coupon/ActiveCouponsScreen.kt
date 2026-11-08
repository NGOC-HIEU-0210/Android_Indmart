package com.hieu.ui.user.coupon

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CouponDto
import com.hieu.ui.components.IndMartEmptyState
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveCouponsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CouponsViewModel = hiltViewModel()
) {
    val state by viewModel.couponsState.collectAsState()
    val context = LocalContext.current

    val copyToClipboard: (String) -> Unit = { code ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Coupon Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Đã sao chép mã $code vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mã giảm giá đang có") },
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
            when (val couponsState = state) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải danh sách mã giảm giá...")
                }
                is UiState.Empty -> {
                    IndMartEmptyState(
                        message = "Hiện chưa có mã giảm giá nào đang diễn ra.",
                        actionText = "Quay lại",
                        onActionClick = onNavigateBack
                    )
                }
                is UiState.Error -> {
                    IndMartErrorMessage(
                        message = couponsState.message,
                        onRetry = viewModel::loadActiveCoupons
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(couponsState.data, key = { it.id ?: 0L }) { coupon ->
                            CouponCardItem(
                                coupon = coupon,
                                onCopyClick = { copyToClipboard(coupon.code) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CouponCardItem(
    coupon: CouponDto,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = coupon.code,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                TextButton(onClick = onCopyClick) {
                    Text("Sao chép mã")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = coupon.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            if (!coupon.description.isNullOrBlank()) {
                Text(
                    text = coupon.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val discountLabel = if (coupon.discountType == "PERCENT") {
                "Giảm ${coupon.discountValue.toInt()}%${if ((coupon.maxDiscount ?: 0.0) > 0) " (Tối đa ${Formatters.formatVnd(coupon.maxDiscount)})" else ""}"
            } else {
                "Giảm ${Formatters.formatVnd(coupon.discountValue)}"
            }

            Text(
                text = discountLabel,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            if ((coupon.minOrderValue ?: 0.0) > 0) {
                Text(
                    text = "Áp dụng cho đơn từ ${Formatters.formatVnd(coupon.minOrderValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!coupon.endDate.isNullOrBlank()) {
                Text(
                    text = "Hạn dùng: ${Formatters.formatDate(coupon.endDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

