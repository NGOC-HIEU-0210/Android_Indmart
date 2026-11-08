package com.hieu.ui.admin.user

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.BuildConfig
import com.hieu.core.theme.PrimaryGreen
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminUserEditViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val context = LocalContext.current

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect { success ->
            if (success) {
                Toast.makeText(
                    context,
                    if (state.isNew) "Tạo người dùng thành công" else "Cập nhật người dùng thành công",
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isNew) "Thêm người dùng mới" else "Sửa người dùng",
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
            Spacer(modifier = Modifier.height(8.dp))

            // Avatar Upload Section (nếu là sửa)
            if (!state.isNew) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarUrl = state.avatar?.let {
                        if (it.startsWith("http")) it else "${BuildConfig.BASE_URL.removeSuffix("/")}/$it"
                    }

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable { avatarLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Tải ảnh đại diện",
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        if (state.isUploadingAvatar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chạm để đổi ảnh đại diện",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Tên đăng nhập
            IndMartTextField(
                value = state.username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = "Tên đăng nhập (*)",
                placeholder = "Ví dụ: user123",
                enabled = state.isNew
            )

            // Email
            IndMartTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Địa chỉ Email (*)",
                placeholder = "user@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Mật khẩu (chỉ hiển thị khi tạo mới)
            if (state.isNew) {
                IndMartTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = "Mật khẩu ban đầu (*)",
                    placeholder = "Tối thiểu 6 ký tự",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IndMartTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onLastNameChange(it) },
                    label = "Họ đệm",
                    placeholder = "Nguyễn Văn",
                    modifier = Modifier.weight(1f)
                )
                IndMartTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onFirstNameChange(it) },
                    label = "Tên",
                    placeholder = "An",
                    modifier = Modifier.weight(1f)
                )
            }

            IndMartTextField(
                value = state.mobile,
                onValueChange = { viewModel.onMobileChange(it) },
                label = "Số điện thoại",
                placeholder = "0987654321",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            IndMartTextField(
                value = state.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = "Địa chỉ nhận hàng",
                placeholder = "Số nhà, tên đường, phường/xã...",
                singleLine = false,
                maxLines = 2
            )

            // Phân quyền (Roles)
            Text("Phân quyền tài khoản:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = state.roles.contains("USER"),
                    onClick = { viewModel.toggleRole("USER") },
                    label = { Text("Người dùng (USER)") }
                )
                FilterChip(
                    selected = state.roles.contains("ADMIN"),
                    onClick = { viewModel.toggleRole("ADMIN") },
                    label = { Text("Quản trị viên (ADMIN)") }
                )
            }

            // Trạng thái tài khoản
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kích hoạt tài khoản",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = state.status,
                    onCheckedChange = { viewModel.onStatusChange(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            IndMartButton(
                text = if (state.isNew) "Tạo người dùng mới" else "Lưu thông tin người dùng",
                onClick = { viewModel.saveUser() },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

