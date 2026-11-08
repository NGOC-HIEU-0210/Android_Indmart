package com.hieu.ui.admin.batch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
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
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.WasteReportItemDto
import com.hieu.ui.components.EmptyContent
import com.hieu.ui.components.ErrorRetryView
import com.hieu.ui.components.LoadingView
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWasteReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminWasteReportViewModel = hiltViewModel()
) {
    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()
    val reportState by viewModel.reportState.collectAsState()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val fromDatePickerState = rememberDatePickerState()
    val toDatePickerState = rememberDatePickerState()

    if (showFromPicker) {
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fromDatePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            viewModel.onFromDateChanged(date.toString())
                        }
                        showFromPicker = false
                    }
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = fromDatePickerState)
        }
    }

    if (showToPicker) {
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        toDatePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            viewModel.onToDateChanged(date.toString())
                        }
                        showToPicker = false
                    }
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = toDatePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo hao hụt & hủy hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Range Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fromDate,
                            onValueChange = { viewModel.onFromDateChanged(it) },
                            label = { Text("Từ ngày") },
                            trailingIcon = {
                                IconButton(onClick = { showFromPicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = toDate,
                            onValueChange = { viewModel.onToDateChanged(it) },
                            label = { Text("Đến ngày") },
                            trailingIcon = {
                                IconButton(onClick = { showToPicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = { viewModel.loadReport() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tra cứu báo cáo")
                    }
                }
            }

            // Report Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = reportState) {
                    is UiState.Loading -> LoadingView()
                    is UiState.Error -> ErrorRetryView(
                        message = state.message,
                        onRetry = { viewModel.loadReport() }
                    )
                    is UiState.Empty -> EmptyContent(
                        message = "Không có số liệu hao hụt trong khoảng thời gian này",
                        actionLabel = "Tải lại",
                        onAction = { viewModel.loadReport() }
                    )
                    is UiState.Success -> {
                        val report = state.data
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Tổng Quan Thẻ Số Liệu
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Tổng lượng hủy", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            Text(
                                                text = "${report.totalWastedQuantity} đơn vị",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(40.dp)
                                                .background(Color.LightGray)
                                        )

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Tổng thiệt hại", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            Text(
                                                text = Formatters.formatVnd(report.totalWastedCost),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Chi tiết mặt hàng (${report.items.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (report.items.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Không có mặt hàng nào bị hủy", color = Color.Gray)
                                    }
                                }
                            } else {
                                items(report.items, key = { it.productId }) { item ->
                                    WasteItemCard(item = item)
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
private fun WasteItemCard(item: WasteReportItemDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "ID: ${item.productId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.wastedQuantity} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = Formatters.formatVnd(item.wastedCost),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

