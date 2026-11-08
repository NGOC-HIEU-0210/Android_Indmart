package com.hieu.ui.admin.batch

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartTextField
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBatchReceiveScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminBatchReceiveViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        viewModel.receiveSuccess.collect { success ->
            if (success) {
                Toast.makeText(context, "Nhập lô hàng thành công", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onExpiryDateChange(localDate.toString())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhập lô hàng mới", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ID Sản phẩm
            IndMartTextField(
                value = state.productId,
                onValueChange = { viewModel.onProductIdChange(it) },
                label = "ID Sản phẩm (*)",
                placeholder = "Nhập mã ID sản phẩm...",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Hiển thị tên sản phẩm tra cứu được
            if (state.isFetchingProduct) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang kiểm tra sản phẩm...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else if (!state.productName.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGreen.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Sản phẩm: ${state.productName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen
                    )
                }
            }

            // Số lượng nhập
            IndMartTextField(
                value = state.quantity,
                onValueChange = { viewModel.onQuantityChange(it) },
                label = "Số lượng nhập (*)",
                placeholder = "Ví dụ: 50, 100...",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Giá vốn
            IndMartTextField(
                value = state.costPrice,
                onValueChange = { viewModel.onCostPriceChange(it) },
                label = "Giá vốn mỗi đơn vị (đ) (*)",
                placeholder = "Ví dụ: 15000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Hạn sử dụng
            OutlinedTextField(
                value = state.expiryDate,
                onValueChange = { viewModel.onExpiryDateChange(it) },
                label = { Text("Ngày hết hạn (YYYY-MM-DD)") },
                placeholder = { Text("Chọn ngày hết hạn của lô") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            IndMartButton(
                text = "Xác nhận nhập kho",
                onClick = { viewModel.submitBatch() },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

