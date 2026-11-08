package com.hieu.ui.user.product

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.core.theme.DiscountBadgeColor
import com.hieu.core.theme.ExpiringBadgeColor
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.ProductDto
import com.hieu.ui.components.IndMartButton
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onCartUpdated: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cartAddedEvent.collect {
            onCartUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavourite) {
                        Icon(
                            imageVector = if (state.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Yêu thích",
                            tint = if (state.isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (state.productState is UiState.Success) {
                val product = (state.productState as UiState.Success<ProductDto>).data
                val isOutOfStock = (product.stock ?: 0) <= 0 || product.active == false

                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Bộ chọn số lượng
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            TextButton(
                                onClick = viewModel::decrementQuantity,
                                enabled = state.quantity > 1 && !isOutOfStock,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.titleLarge)
                            }

                            Text(
                                text = "${state.quantity}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            TextButton(
                                onClick = viewModel::incrementQuantity,
                                enabled = !isOutOfStock,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.titleLarge)
                            }
                        }

                        // Nút thêm vào giỏ
                        IndMartButton(
                            text = if (isOutOfStock) "Hết hàng" else "Thêm vào giỏ",
                            onClick = { viewModel.addToCart(onRequireLogin = onNavigateToLogin) },
                            enabled = !isOutOfStock,
                            isLoading = state.isAddingToCart,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val productState = state.productState) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải thông tin sản phẩm...")
                }
                is UiState.Error -> {
                    IndMartErrorMessage(
                        message = productState.message,
                        onRetry = viewModel::loadProductDetail
                    )
                }
                is UiState.Success -> {
                    val product = productState.data
                    val allImages = buildList {
                        if (!product.image.isNullOrBlank()) add(product.image)
                        product.images.forEach { img ->
                            if (!img.imageUrl.isNullOrBlank()) add(img.imageUrl)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Thư viện ảnh (Pager)
                        val pagerState = rememberPagerState(pageCount = { maxOf(allImages.size, 1) })

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val imageUrl = allImages.getOrNull(page)
                                AsyncImage(
                                    model = Formatters.toFullImageUrl(imageUrl),
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Huy hiệu giảm giá
                            if (product.hasDiscount) {
                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.TopStart)
                                        .background(DiscountBadgeColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Giảm ${product.discountPercent}%",
                                        color = MaterialTheme.colorScheme.onError,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Chấm chỉ mục trang ảnh
                            if (allImages.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(allImages.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (pagerState.currentPage == index)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        // Thông tin chi tiết sản phẩm
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Tên sản phẩm
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Danh mục & Yêu thích
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!product.categoryName.isNullOrBlank()) {
                                    Text(
                                        text = "Danh mục: ${product.categoryName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${product.favouriteCount ?: 0} lượt thích",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Giá
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = Formatters.formatVnd(product.effectivePrice),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                if (product.hasDiscount && product.price > product.effectivePrice) {
                                    Text(
                                        text = Formatters.formatVnd(product.price),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            textDecoration = TextDecoration.LineThrough,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Thông tin Hạn sử dụng & Kho
                            Text(
                                text = "Thông tin tươi ngon",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (product.shelfLifeDays != null) {
                                        Text("Hạn sử dụng khuyến nghị: ${product.shelfLifeDays} ngày")
                                    }
                                    if (!product.nearestExpiry.isNullOrBlank()) {
                                        Text("Hạn dùng gần nhất: ${Formatters.formatDate(product.nearestExpiry)}")
                                    }
                                    if (product.daysLeft != null) {
                                        Text("Thời gian còn lại: ${product.daysLeft} ngày")
                                    }
                                    Text("Tình trạng kho: ${if ((product.stock ?: 0) > 0) "Còn ${product.stock} sản phẩm" else "Tạm hết hàng"}")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Mô tả sản phẩm
                            if (!product.description.isNullOrBlank()) {
                                Text(
                                    text = "Mô tả sản phẩm",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = product.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
                UiState.Empty -> Unit
            }
        }
    }
}

