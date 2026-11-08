package com.hieu.ui.admin.coupon

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartTextField
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminCouponEditViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val context = LocalContext.current
    val isEditMode = state.id != null

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect { success ->
            if (success) {
                Toast.makeText(
                    context,
                    if (isEditMode) "Cập nhật mã giảm giá thành công" else "Tạo mã giảm giá thành công",
                    Toast.LENGTH_SHORT
                ).show()
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

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onStartDateChange(localDate.toString())
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onEndDateChange(localDate.toString())
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Sửa mã giảm giá" else "Thêm mã giảm giá",
                        fontWeight = FontWeight.Bold
                    )
                },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Mã code
            IndMartTextField(
                value = state.code,
                onValueChange = { viewModel.onCodeChange(it) },
                label = "Mã giảm giá (ví dụ: IND20K, VEGGIE10)",
                placeholder = "Nhập mã viết hoa...",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )

            // Tên
            IndMartTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = "Tên chương trình khuyến mãi",
                placeholder = "Ví dụ: Giảm giá mùa hè"
            )

            // Mô tả
            IndMartTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = "Mô tả chi tiết",
                placeholder = "Áp dụng cho mọi đơn hàng...",
                singleLine = false,
                maxLines = 3
            )

            // Loại giảm giá (PERCENT / FIXED)
            Text("Loại giảm giá:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = state.discountType == "PERCENT",
                    onClick = { viewModel.onDiscountTypeChange("PERCENT") },
                    label = { Text("Theo phần trăm (%)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.discountType == "FIXED",
                    onClick = { viewModel.onDiscountTypeChange("FIXED") },
                    label = { Text("Số tiền cố định (đ)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Giá trị giảm
            IndMartTextField(
                value = state.discountValue,
                onValueChange = { viewModel.onDiscountValueChange(it) },
                label = if (state.discountType == "PERCENT") "Phần trăm giảm (%)" else "Số tiền giảm (đ)",
                placeholder = if (state.discountType == "PERCENT") "Ví dụ: 10, 15" else "Ví dụ: 20000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Giảm tối đa nếu là phần trăm
            if (state.discountType == "PERCENT") {
                IndMartTextField(
                    value = state.maxDiscount,
                    onValueChange = { viewModel.onMaxDiscountChange(it) },
                    label = "Giảm tối đa (đ) (không bắt buộc)",
                    placeholder = "Ví dụ: 50000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Đơn tối thiểu
            IndMartTextField(
                value = state.minOrderValue,
                onValueChange = { viewModel.onMinOrderValueChange(it) },
                label = "Giá trị đơn tối thiểu (đ) (không bắt buộc)",
                placeholder = "Ví dụ: 100000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Giới hạn số lượt dùng
            IndMartTextField(
                value = state.usageLimit,
                onValueChange = { viewModel.onUsageLimitChange(it) },
                label = "Số lượng mã (để trống nếu không giới hạn)",
                placeholder = "Ví dụ: 100",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Ngày bắt đầu
            OutlinedTextField(
                value = state.startDate,
                onValueChange = { viewModel.onStartDateChange(it) },
                label = { Text("Ngày bắt đầu (YYYY-MM-DD)") },
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày bắt đầu")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Ngày kết thúc
            OutlinedTextField(
                value = state.endDate,
                onValueChange = { viewModel.onEndDateChange(it) },
                label = { Text("Ngày kết thúc (YYYY-MM-DD)") },
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày kết thúc")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Trạng thái hoạt động
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kích hoạt mã giảm giá",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = state.active,
                    onCheckedChange = { viewModel.onActiveChange(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            IndMartButton(
                text = if (isEditMode) "Lưu thay đổi" else "Tạo mã giảm giá",
                onClick = { viewModel.saveCoupon() },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

