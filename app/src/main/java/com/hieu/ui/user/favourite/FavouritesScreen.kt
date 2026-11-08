package com.hieu.ui.user.favourite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hieu.core.util.Formatters
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.FavouriteDto
import com.hieu.ui.components.IndMartEmptyState
import com.hieu.ui.components.IndMartErrorMessage
import com.hieu.ui.components.IndMartLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    onNavigateBack: () -> Unit,
    onProductClick: (Long) -> Unit,
    onLoginClick: () -> Unit,
    viewModel: FavouritesViewModel = hiltViewModel()
) {
    val favouritesState by viewModel.favouritesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sản phẩm yêu thích") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!viewModel.isLoggedIn()) {
                IndMartEmptyState(
                    message = "Vui lòng đăng nhập để xem danh sách yêu thích của bạn.",
                    actionText = "Đăng nhập ngay",
                    onActionClick = onLoginClick
                )
                return@Box
            }

            when (val state = favouritesState) {
                is UiState.Loading -> {
                    IndMartLoadingIndicator(message = "Đang tải danh sách yêu thích...")
                }
                is UiState.Empty -> {
                    IndMartEmptyState(
                        message = "Bạn chưa lưu sản phẩm yêu thích nào.",
                        actionText = "Khám phá rau củ quả",
                        onActionClick = onNavigateBack
                    )
                }
                is UiState.Error -> {
                    IndMartErrorMessage(
                        message = state.message,
                        onRetry = viewModel::loadFavourites
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.data, key = { it.id ?: 0L }) { fav ->
                            FavouriteRowItem(
                                fav = fav,
                                onClick = { fav.productId?.let(onProductClick) },
                                onRemove = { fav.productId?.let(viewModel::removeFavourite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavouriteRowItem(
    fav: FavouriteDto,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = Formatters.toFullImageUrl(fav.productImage),
                contentDescription = fav.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fav.productName ?: "Sản phẩm",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Formatters.formatVnd(fav.productPrice),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa khỏi yêu thích",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

