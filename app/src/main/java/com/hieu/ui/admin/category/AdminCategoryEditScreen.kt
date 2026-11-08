package com.hieu.ui.admin.category

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.core.util.Formatters
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminCategoryEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isParentDropdownExpanded by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(context, it) }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "Thêm danh mục mới" else "Chỉnh sửa danh mục") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
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
            // Phần ảnh danh mục
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
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
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!state.image.isNullOrBlank()) {
                            AsyncImage(
                                model = Formatters.toFullImageUrl(state.image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Column {
                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            enabled = !state.isUploadingImage
                        ) {
                            Text(if (state.image == null) "Chọn ảnh" else "Đổi ảnh")
                        }
                        if (state.isNew) {
                            Text(
                                text = "(Lưu thông tin trước khi tải ảnh)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Phần thông tin
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Thông tin danh mục", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    IndMartTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Tên danh mục *",
                        isError = state.nameError != null,
                        errorMessage = state.nameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    // Chọn danh mục cha
                    ExposedDropdownMenuBox(
                        expanded = isParentDropdownExpanded,
                        onExpandedChange = { isParentDropdownExpanded = it }
                    ) {
                        val parentName = state.categories.find { it.id == state.parentCategoryId }?.name ?: "Danh mục gốc (Không có cha)"
                        OutlinedTextField(
                            value = parentName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Danh mục cấp cha") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isParentDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isParentDropdownExpanded,
                            onDismissRequest = { isParentDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Danh mục gốc (Không có cha)") },
                                onClick = {
                                    viewModel.onParentCategoryChange(null)
                                    isParentDropdownExpanded = false
                                }
                            )
                            state.categories
                                .filter { it.id != state.id } // Không chọn chính nó làm cha
                                .forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            viewModel.onParentCategoryChange(cat.id)
                                            isParentDropdownExpanded = false
                                        }
                                    )
                                }
                        }
                    }

                    // Công tắc Kích hoạt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hoạt động", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (state.active) "Danh mục hiển thị cho khách" else "Danh mục đang tạm ẩn",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.active,
                            onCheckedChange = viewModel::onActiveToggle
                        )
                    }
                }
            }

            IndMartButton(
                text = if (state.isNew) "Tạo danh mục" else "Lưu cập nhật",
                onClick = { viewModel.saveCategory(onSuccess = onNavigateBack) },
                isLoading = state.isSaving,
                enabled = !state.isSaving
            )
        }
    }
}

