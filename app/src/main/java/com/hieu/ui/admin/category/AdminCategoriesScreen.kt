package com.hieu.ui.admin.category

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CategoryDto
import com.hieu.ui.components.IndMartEmptyState
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator

@Composable
fun AdminCategoriesScreen(
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Long) -> Unit,
    viewModel: AdminCategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.categoriesState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    var categoryToDelete by remember { mutableStateOf<CategoryDto?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Xác nhận xóa danh mục") },
            text = { Text("Bạn có chắc chắn muốn xóa danh mục \"${categoryToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = categoryToDelete?.id
                        categoryToDelete = null
                        if (id != null) viewModel.deleteCategory(id)
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCategoryClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm danh mục")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val catState = state) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải danh mục...")
                }
                is UiState.Empty -> {
                    IndMartEmptyState(
                        message = "Chưa có danh mục nào trong hệ thống.",
                        actionText = "Tạo danh mục mới",
                        onActionClick = onAddCategoryClick
                    )
                }
                is UiState.Error -> {
                    IndMartErrorMessage(
                        message = catState.message,
                        onRetry = viewModel::loadCategories
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(catState.data, key = { it.id ?: 0L }) { category ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = Formatters.toFullImageUrl(category.image),
                                        contentDescription = category.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )

                                        if (!category.parentCategoryName.isNullOrBlank()) {
                                            Text(
                                                text = "Danh mục cha: ${category.parentCategoryName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Surface(
                                            color = if (category.active == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (category.active == true) "Đang hoạt động" else "Đang ẩn",
                                                color = if (category.active == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = { category.id?.let(onEditCategoryClick) }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { categoryToDelete = category }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

