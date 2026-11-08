package com.hieu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hieu.core.theme.DiscountBadgeColor
import com.hieu.core.theme.ExpiringBadgeColor
import com.hieu.core.util.Formatters
import com.hieu.data.remote.dto.ProductDto

@Composable
fun ProductGridCard(
    product: ProductDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = Formatters.toFullImageUrl(product.image),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Nhãn giảm giá hoặc giải cứu
                if (product.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .background(DiscountBadgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Nhãn số ngày còn lại (nếu có thông tin giải cứu)
                if (product.daysLeft != null && product.daysLeft >= 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomStart)
                            .background(ExpiringBadgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (product.daysLeft == 0) "Hạn hôm nay" else "Còn ${product.daysLeft} ngày",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Hiển thị giá
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = Formatters.formatVnd(product.effectivePrice),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (product.hasDiscount && product.price > product.effectivePrice) {
                        Text(
                            text = Formatters.formatVnd(product.price),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Tồn kho nhỏ
                if (product.stock != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (product.stock > 0) "Còn ${product.stock} phần" else "Hết hàng",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun RescueProductCard(
    product: ProductDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = Formatters.toFullImageUrl(product.image),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (product.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopStart)
                            .background(DiscountBadgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (product.daysLeft != null) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomStart)
                            .background(ExpiringBadgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (product.daysLeft <= 0) "Hết hạn hôm nay" else "Còn ${product.daysLeft} ngày",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Formatters.formatVnd(product.effectivePrice),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                if (product.price > product.effectivePrice) {
                    Text(
                        text = Formatters.formatVnd(product.price),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

