package com.hieu.ui.user.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hieu.core.util.UiState
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator
import com.hieu.ui.components.ProductGridCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onProductClick: (Long) -> Unit,
    onCategoryClick: (Long, String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categoriesState.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pagingProducts = viewModel.productsPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tiêu đề
        TopAppBar(
            title = { Text("Danh mục & Sản phẩm") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Ô tìm kiếm trong danh mục
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = { Text("Tìm theo tên sản phẩm...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa tìm kiếm"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Danh sách Chips danh mục
        when (val state = categoriesState) {
            is UiState.Success -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("Tất cả") }
                        )
                    }
                    items(state.data, key = { it.id ?: 0L }) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
            is UiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            UiState.Empty -> Unit
        }

        // Lưới sản phẩm phân trang
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = pagingProducts.itemCount,
                key = { index -> pagingProducts[index]?.id ?: index.toLong() }
            ) { index ->
                val product = pagingProducts[index]
                if (product != null) {
                    ProductGridCard(
                        product = product,
                        onClick = { product.id?.let(onProductClick) }
                    )
                }
            }

            // Xử lý trạng thái tải trang
            when (val appendState = pagingProducts.loadState.append) {
                is LoadState.Loading -> {
                    item(span = { GridItemSpan(2) }) {
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
                is LoadState.Error -> {
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Lỗi tải sản phẩm",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { pagingProducts.retry() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

