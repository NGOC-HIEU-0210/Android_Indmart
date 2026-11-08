package com.hieu.ui.admin.product

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.core.util.Formatters
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartLoadingIndicator
import com.hieu.ui.components.IndMartTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminProductEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val mainImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadMainImage(context, it) }
    }

    val extraImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadAdditionalImage(context, it) }
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
                title = { Text(if (state.isNew) "Thêm sản phẩm mới" else "Chỉnh sửa sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            IndMartLoadingIndicator(message = "Đang tải thông tin sản phẩm...")
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Phần 1: Ảnh đại diện & Ảnh phụ
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ảnh đại diện chính", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { mainImagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!state.mainImage.isNullOrBlank()) {
                                AsyncImage(
                                    model = Formatters.toFullImageUrl(state.mainImage),
                                    contentDescription = "Main Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column {
                            OutlinedButton(
                                onClick = { mainImagePicker.launch("image/*") },
                                enabled = !state.isUploadingImage
                            ) {
                                Text(if (state.mainImage == null) "Chọn ảnh chính" else "Đổi ảnh chính")
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

                    // Danh sách ảnh phụ
                    if (!state.isNew) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ảnh phụ bổ sung (${state.images.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            TextButton(onClick = { extraImagePicker.launch("image/*") }) {
                                Text("+ Thêm ảnh phụ")
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.images, key = { it.id ?: 0L }) { img ->
                                Box(modifier = Modifier.size(72.dp)) {
                                    AsyncImage(
                                        model = Formatters.toFullImageUrl(img.imageUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                    IconButton(
                                        onClick = { img.id?.let(viewModel::deleteAdditionalImage) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Phần 2: Thông tin cơ bản
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Thông tin sản phẩm", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    IndMartTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Tên sản phẩm *",
                        isError = state.nameError != null,
                        errorMessage = state.nameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    IndMartTextField(
                        value = state.price,
                        onValueChange = viewModel::onPriceChange,
                        label = "Giá bán (VND) *",
                        placeholder = "Ví dụ: 35000",
                        isError = state.priceError != null,
                        errorMessage = state.priceError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )

                    // Chọn danh mục (ExposedDropdownMenuBox)
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = it }
                    ) {
                        val selectedCategoryName = state.categories.find { it.id == state.categoryId }?.name ?: "Chưa chọn danh mục"
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Danh mục sản phẩm") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Không thuộc danh mục") },
                                onClick = {
                                    viewModel.onCategoryChange(null)
                                    isCategoryDropdownExpanded = false
                                }
                            )
                            state.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.onCategoryChange(category.id)
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    IndMartTextField(
                        value = state.shelfLifeDays,
                        onValueChange = viewModel::onShelfLifeDaysChange,
                        label = "Hạn bảo quản (Số ngày)",
                        placeholder = "Ví dụ: 5",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )

                    IndMartTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = "Mô tả sản phẩm",
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    // Công tắc Kích hoạt (Active)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hiển thị bán trên ứng dụng", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (state.active) "Sản phẩm đang được mở bán" else "Sản phẩm tạm thời bị ẩn",
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

            // Nút Lưu
            IndMartButton(
                text = if (state.isNew) "Tạo sản phẩm" else "Lưu cập nhật",
                onClick = { viewModel.saveProduct(onSuccess = { onNavigateBack() }) },
                isLoading = state.isSaving,
                enabled = !state.isSaving
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

