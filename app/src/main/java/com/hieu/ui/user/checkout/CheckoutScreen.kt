package com.hieu.ui.user.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CartDto
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderPlacedSuccess: (orderCode: String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.orderSuccessEvent.collect { order ->
            onOrderPlacedSuccess(order.orderCode ?: "${order.id}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (state.cartState is UiState.Success) {
                val cart = (state.cartState as UiState.Success<CartDto>).data
                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng thanh toán:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                Formatters.formatVnd(cart.subtotal),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        IndMartButton(
                            text = "Xác nhận đặt hàng",
                            onClick = viewModel::submitOrder,
                            isLoading = state.isSubmitting,
                            enabled = !state.isSubmitting
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thông báo lỗi chung nếu có
            if (!state.generalError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.generalError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Phần 1: Thông tin người nhận
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Thông tin giao hàng",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    IndMartTextField(
                        value = state.customerName,
                        onValueChange = viewModel::onNameChange,
                        label = "Họ và tên người nhận *",
                        isError = state.nameError != null,
                        errorMessage = state.nameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    IndMartTextField(
                        value = state.phone,
                        onValueChange = viewModel::onPhoneChange,
                        label = "Số điện thoại *",
                        isError = state.phoneError != null,
                        errorMessage = state.phoneError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    IndMartTextField(
                        value = state.address,
                        onValueChange = viewModel::onAddressChange,
                        label = "Địa chỉ nhận hàng chi tiết *",
                        isError = state.addressError != null,
                        errorMessage = state.addressError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    IndMartTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email nhận thông báo (tùy chọn)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    IndMartTextField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        label = "Ghi chú đơn hàng (tùy chọn)",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }

            // Phần 2: Phương thức thanh toán
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Phương thức thanh toán",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onPaymentMethodChange("COD") }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = state.paymentMethod == "COD",
                            onClick = { viewModel.onPaymentMethodChange("COD") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Thanh toán khi nhận hàng (COD)", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Thanh toán bằng tiền mặt khi shipper giao hàng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onPaymentMethodChange("BANK_TRANSFER") }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = state.paymentMethod == "BANK_TRANSFER",
                            onClick = { viewModel.onPaymentMethodChange("BANK_TRANSFER") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Chuyển khoản ngân hàng", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Chuyển khoản qua mã QR / số tài khoản",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Phần 3: Danh sách tóm tắt các món trong giỏ
            if (state.cartState is UiState.Success) {
                val cart = (state.cartState as UiState.Success<CartDto>).data
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kiểm tra sản phẩm (${cart.totalQuantity} món)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        cart.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.productName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = Formatters.formatVnd(item.subtotal),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

