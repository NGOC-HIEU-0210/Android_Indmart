package com.hieu.ui.user.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.CategoryDto
import com.hieu.data.remote.dto.ProductDto
import com.hieu.ui.components.CategoryCircleItem
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.ProductGridCard
import com.hieu.ui.components.RescueProductCard

@Composable
fun HomeScreen(
    onProductClick: (Long) -> Unit,
    onCategoryClick: (Long, String) -> Unit,
    onCartUpdated: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val rescueState by viewModel.rescueState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pagingProducts = viewModel.productsPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Thanh tìm kiếm
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Tìm kiếm rau củ quả...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa tìm kiếm"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Nếu người dùng không tìm kiếm, hiển thị mục "Giải cứu hôm nay" và "Danh mục"
            if (searchQuery.isBlank()) {
                // Section: GIẢI CỨU HÔM NAY
                item(span = { GridItemSpan(2) }) {
                    when (val state = rescueState) {
                        is UiState.Success -> {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Giải cứu hôm nay",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                    Text(
                                        text = "Sắp hết hạn - Giảm sâu",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.data, key = { it.id ?: 0L }) { product ->
                                        RescueProductCard(
                                            product = product,
                                            onClick = { product.id?.let(onProductClick) }
                                        )
                                    }
                                }
                            }
                        }
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                        else -> Unit
                    }
                }

                // Section: DANH MỤC
                item(span = { GridItemSpan(2) }) {
                    when (val state = categoriesState) {
                        is UiState.Success -> {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                Text(
                                    text = "Danh mục sản phẩm",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.data, key = { it.id ?: 0L }) { category ->
                                        CategoryCircleItem(
                                            category = category,
                                            onClick = {
                                                category.id?.let { id ->
                                                    onCategoryClick(id, category.name)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        else -> Unit
                    }
                }

                // Tiêu đề Sản phẩm mới
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Sản phẩm tươi mới",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
                    )
                }
            } else {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Kết quả tìm kiếm cho \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                }
            }

            // Danh sách sản phẩm phân trang (Paging 3)
            items(
                count = pagingProducts.itemCount,
                key = { index -> pagingProducts[index]?.id ?: index.toLong() }
            ) { index ->
                val product = pagingProducts[index]
                if (product != null) {
                    Box(
                        modifier = Modifier.padding(
                            start = if (index % 2 == 0) 16.dp else 4.dp,
                            end = if (index % 2 == 1) 16.dp else 4.dp
                        )
                    ) {
                        ProductGridCard(
                            product = product,
                            onClick = { product.id?.let(onProductClick) }
                        )
                    }
                }
            }

            // Trạng thái Paging tải trang tiếp theo hoặc lỗi
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
                                text = "Lỗi tải thêm sản phẩm: ${appendState.error.localizedMessage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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

