package com.hieu.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.core.theme.DiscountBadgeColor
import com.hieu.core.theme.ExpiringBadgeColor
import com.hieu.core.theme.SuccessGreen
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.DashboardDto
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val dashState = state) {
            is UiState.Loading -> {
                IndMartLoadingIndicator(message = "Đang tải dữ liệu tổng quan...")
            }
            is UiState.Error -> {
                IndMartErrorMessage(
                    message = dashState.message,
                    onRetry = viewModel::loadDashboardStats
                )
            }
            is UiState.Success -> {
                val stats = dashState.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tổng quan hoạt động IND MART",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    // Thẻ Doanh thu
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Doanh thu hoàn thành",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Formatters.formatVnd(stats.totalRevenue),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Nhóm Đơn hàng
                    Text(
                        text = "Tình trạng đơn hàng",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Tổng đơn",
                            value = "${stats.totalOrders}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Chờ xử lý",
                            value = "${stats.pendingOrders}",
                            valueColor = ExpiringBadgeColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Hoàn thành",
                            value = "${stats.completedOrders}",
                            valueColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Đã hủy",
                            value = "${stats.cancelledOrders}",
                            valueColor = DiscountBadgeColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Nhóm Danh mục & Sản phẩm & Người dùng
                    Text(
                        text = "Tài nguyên hệ thống",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Sản phẩm",
                            value = "${stats.totalProducts}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Danh mục",
                            value = "${stats.totalCategories}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Mã giảm giá",
                            value = "${stats.totalCoupons}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Người dùng",
                            value = "${stats.totalUsers}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            UiState.Empty -> Unit
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            )
        }
    }
}

