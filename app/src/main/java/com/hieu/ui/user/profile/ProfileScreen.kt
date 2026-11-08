package com.hieu.ui.user.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.UserDto
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartEmptyState
import com.hieu.ui.components.IndMartLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOrdersClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    onCouponsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    onAdminPortalClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLoggedOut = onLoginClick)
                    }
                ) {
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cá nhân") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!session.isLoggedIn) {
                IndMartEmptyState(
                    message = "Bạn chưa đăng nhập vào hệ thống IND MART.",
                    actionText = "Đăng nhập ngay",
                    onActionClick = onLoginClick
                )
                return@Box
            }

            when (val userState = state.userState) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải thông tin cá nhân...")
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(userState.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = viewModel::loadProfile) {
                            Text("Tải lại")
                        }
                    }
                }
                is UiState.Success -> {
                    val user = userState.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Thẻ đại diện cá nhân
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { photoPickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!user.avatar.isNullOrBlank()) {
                                        AsyncImage(
                                            model = Formatters.toFullImageUrl(user.avatar),
                                            contentDescription = "Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = user.displayName.take(1).uppercase(),
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    if (state.isUploadingAvatar) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "@${user.username ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!user.email.isNullOrBlank()) {
                                        Text(
                                            text = user.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    TextButton(
                                        onClick = { photoPickerLauncher.launch("image/*") },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Đổi ảnh đại diện", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // Danh sách menu thao tác
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                ProfileMenuItem(
                                    title = "Sửa hồ sơ cá nhân",
                                    subtitle = "Cập nhật họ tên, số điện thoại, địa chỉ",
                                    onClick = onEditProfileClick
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                ProfileMenuItem(
                                    title = "Đơn hàng của tôi",
                                    subtitle = "Xem lịch sử và trạng thái giao hàng",
                                    onClick = onOrdersClick
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                ProfileMenuItem(
                                    title = "Sản phẩm yêu thích",
                                    subtitle = "Danh sách các loại rau củ bạn quan tâm",
                                    onClick = onFavouritesClick
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                ProfileMenuItem(
                                    title = "Mã giảm giá đang có",
                                    subtitle = "Khám phá các ưu đãi mua sắm hấp dẫn",
                                    onClick = onCouponsClick
                                )

                                // Nếu tài khoản có quyền ADMIN
                                if (session.isAdmin) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    ProfileMenuItem(
                                        title = "Cổng Quản trị viên (Admin)",
                                        subtitle = "Chuyển sang giao diện quản lý hệ thống IND MART",
                                        onClick = onAdminPortalClick,
                                        isHighlighted = true
                                    )
                                }
                            }
                        }

                        // Nút Đăng xuất
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Đăng xuất tài khoản")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                UiState.Empty -> Unit
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

