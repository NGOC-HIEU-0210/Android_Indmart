package com.hieu.ui.admin.batch

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.core.theme.PrimaryGreen
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.BatchDto
import com.hieu.ui.components.EmptyContent
import com.hieu.ui.components.ErrorRetryView
import com.hieu.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBatchesScreen(
    onNavigateToReceiveBatch: () -> Unit,
    onNavigateToWasteReport: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: AdminBatchesViewModel = hiltViewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val expiringDays by viewModel.expiringDays.collectAsState()
    val batchesState by viewModel.batchesState.collectAsState()
    val productIdInput by viewModel.productIdInput.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isWritingOff by viewModel.isWritingOff.collectAsState()
    val context = LocalContext.current

    var showWriteOffDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (showWriteOffDialog) {
        AlertDialog(
            onDismissRequest = { showWriteOffDialog = false },
            title = { Text("Xuất hủy lô hàng hết hạn") },
            text = { Text("Hệ thống sẽ quét và chuyển toàn bộ số lượng còn tồn của các lô đã quá hạn sử dụng sang trạng thái ĐÃ HỦY (EXPIRED). Bạn có chắc chắn muốn thực hiện?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWriteOffDialog = false
                        viewModel.writeOffExpiredBatches()
                    }
                ) {
                    Text("Xuất hủy", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWriteOffDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kho & Lô hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onOpenDrawer) {
                        Text("Menu", fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToWasteReport) {
                        Icon(Icons.Default.Assessment, contentDescription = "Báo cáo hao hụt")
                    }
                    IconButton(onClick = onNavigateToReceiveBatch) {
                        Icon(Icons.Default.Add, contentDescription = "Nhập lô mới")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReceiveBatch,
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nhập lô mới")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selection
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = currentTab == BatchTab.EXPIRING,
                    onClick = { viewModel.selectTab(BatchTab.EXPIRING) },
                    text = { Text("Sắp hết hạn") }
                )
                Tab(
                    selected = currentTab == BatchTab.BY_PRODUCT,
                    onClick = { viewModel.selectTab(BatchTab.BY_PRODUCT) },
                    text = { Text("Theo sản phẩm") }
                )
            }

            // Controls under tab
            if (currentTab == BatchTab.EXPIRING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 7).forEach { days ->
                            FilterChip(
                                selected = expiringDays == days,
                                onClick = { viewModel.onExpiringDaysChanged(days) },
                                label = { Text("$days ngày") }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showWriteOffDialog = true },
                        enabled = !isWritingOff,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xuất hủy", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = productIdInput,
                        onValueChange = { viewModel.onProductIdChanged(it) },
                        label = { Text("Nhập ID sản phẩm") },
                        placeholder = { Text("Ví dụ: 1, 2...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = { viewModel.searchByProduct() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tìm")
                    }
                }
            }

            // Batches List
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = batchesState) {
                    is UiState.Loading -> LoadingView()
                    is UiState.Error -> ErrorRetryView(
                        message = state.message,
                        onRetry = {
                            if (currentTab == BatchTab.EXPIRING) {
                                viewModel.loadExpiringBatches(expiringDays)
                            } else {
                                viewModel.searchByProduct()
                            }
                        }
                    )
                    is UiState.Empty -> EmptyContent(
                        message = if (currentTab == BatchTab.EXPIRING) "Không có lô hàng nào sắp hết hạn" else "Không tìm thấy lô hàng nào cho sản phẩm này",
                        actionLabel = "Nhập lô hàng mới",
                        onAction = onNavigateToReceiveBatch
                    )
                    is UiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data, key = { it.id ?: 0L }) { batch ->
                                BatchCard(batch = batch)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatchCard(batch: BatchDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = batch.productName?.takeIf { it.isNotBlank() } ?: "Lô sản phẩm #${batch.productId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val status = batch.status ?: "ACTIVE"
                val (bgCol, textCol, label) = when (status) {
                    "ACTIVE" -> Triple(PrimaryGreen.copy(alpha = 0.15f), PrimaryGreen, "Còn hàng")
                    "SOLD_OUT" -> Triple(Color.LightGray.copy(alpha = 0.3f), Color.DarkGray, "Hết hàng")
                    "EXPIRED" -> Triple(Color.Red.copy(alpha = 0.15f), Color.Red, "Hết hạn")
                    else -> Triple(Color.LightGray.copy(alpha = 0.3f), Color.DarkGray, status)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bgCol)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textCol
                    )
                }
            }

            Text(
                text = "Mã lô: #${batch.id} | ID sản phẩm: ${batch.productId}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tồn: ${batch.remainingQuantity ?: 0} / ${batch.initialQuantity ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if ((batch.wastedQuantity ?: 0) > 0) {
                    Text(
                        text = "Đã hủy: ${batch.wastedQuantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            batch.costPrice?.let {
                Text(
                    text = "Giá vốn: ${Formatters.formatVnd(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hạn dùng: ${batch.expiryDate ?: "Không có"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                batch.daysLeft?.let { days ->
                    val color = when {
                        days <= 0 -> Color.Red
                        days <= 2 -> Color(0xFFE65100)
                        else -> PrimaryGreen
                    }
                    Text(
                        text = if (days <= 0) "Đã hết hạn" else "Còn $days ngày",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}

