package com.hieu.ui.admin.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.hieu.BuildConfig
import com.hieu.core.theme.PrimaryGreen
import com.hieu.data.remote.dto.UserDto
import com.hieu.ui.components.EmptyContent
import com.hieu.ui.components.ErrorRetryView
import com.hieu.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    onNavigateToAddUser: () -> Unit,
    onNavigateToEditUser: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val users = viewModel.usersStream.collectAsLazyPagingItems()
    val context = LocalContext.current

    var userToDelete by remember { mutableStateOf<UserDto?>(null) }
    var userToToggle by remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
            users.refresh()
        }
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Xác nhận xóa người dùng") },
            text = { Text("Bạn có chắc muốn xóa tài khoản '${userToDelete?.username}' không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToDelete?.let { viewModel.deleteUser(it) }
                        userToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (userToToggle != null) {
        val willLock = userToToggle?.status ?: true
        AlertDialog(
            onDismissRequest = { userToToggle = null },
            title = { Text(if (willLock) "Xác nhận khóa tài khoản" else "Xác nhận mở khóa tài khoản") },
            text = { Text("Bạn có muốn ${if (willLock) "khóa" else "mở khóa"} người dùng '${userToToggle?.username}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToToggle?.let { viewModel.toggleUserStatus(it) }
                        userToToggle = null
                    }
                ) {
                    Text(if (willLock) "Khóa" else "Mở khóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToToggle = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý người dùng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onOpenDrawer) {
                        Text("Menu", fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddUser) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Thêm người dùng")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddUser,
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Thêm người dùng")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { viewModel.onKeywordChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm theo username, họ tên, email, SĐT...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchKeyword.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onKeywordChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Users List
            Box(modifier = Modifier.fillMaxSize()) {
                if (users.loadState.refresh is LoadState.Loading) {
                    LoadingView()
                } else if (users.loadState.refresh is LoadState.Error) {
                    val error = users.loadState.refresh as LoadState.Error
                    ErrorRetryView(
                        message = error.error.localizedMessage ?: "Lỗi tải danh sách người dùng",
                        onRetry = { users.retry() }
                    )
                } else if (users.itemCount == 0) {
                    EmptyContent(
                        message = "Không tìm thấy người dùng nào",
                        actionLabel = "Tải lại",
                        onAction = { users.refresh() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(users.itemCount) { index ->
                            users[index]?.let { user ->
                                UserItemCard(
                                    user = user,
                                    isSelf = user.username == viewModel.currentUsername,
                                    onEdit = { user.id?.let { onNavigateToEditUser(it) } },
                                    onToggleStatus = { userToToggle = user },
                                    onDelete = { userToDelete = user }
                                )
                            }
                        }

                        if (users.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
private fun UserItemCard(
    user: UserDto,
    isSelf: Boolean,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                val avatarUrl = user.avatar?.let {
                    if (it.startsWith("http")) it else "${BuildConfig.BASE_URL.removeSuffix("/")}/$it"
                }

                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = user.username,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user.username?.take(1) ?: "U").uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.username ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSelf) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1976D2).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Bạn", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )

                    user.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Status Badge
                val isActive = user.status ?: true
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isActive) PrimaryGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isActive) "Hoạt động" else "Đã khóa",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) PrimaryGreen else Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Roles & Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    user.roles.forEach { role ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(role, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                        }
                    }
                }

                user.mobile?.let {
                    Text(text = "SĐT: $it", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryGreen)
                }

                IconButton(
                    onClick = onToggleStatus,
                    enabled = !isSelf
                ) {
                    Icon(
                        if (user.status == false) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Khóa/Mở khóa",
                        tint = if (isSelf) Color.LightGray else Color(0xFFE65100)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !isSelf
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = if (isSelf) Color.LightGray else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

