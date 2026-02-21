package com.samuelokello.feat.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.samuelokello.core.domain.model.Product
import org.koin.androidx.compose.koinViewModel

// Цвета для чайной тематики
private val TeaGreen = Color(0xFF4A7C59)
private val TeaGreenLight = Color(0xFF6B9B7A)
private val TeaBrown = Color(0xFF8B5A2B)
private val TeaCream = Color(0xFFFFF8E7)
private val TeaGold = Color(0xFFD4AF37)
private val BackgroundGradientStart = Color(0xFFF5F1EB)
private val BackgroundGradientEnd = Color(0xFFEDE8E0)

@Composable
fun ProductDetailsScreen(
    viewModel: ProductDetailViewModel = koinViewModel(),
    productId: Int,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(productId) {
        viewModel.getProductById(productId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundGradientStart, BackgroundGradientEnd)
                )
            ),
    ) {
        AnimatedVisibility(
            visible = state is ProductDetailUiState.Loading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ProductLoadingScreen()
        }

        AnimatedVisibility(
            visible = state is ProductDetailUiState.Success,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400)
            ),
            exit = fadeOut(),
        ) {
            if (state is ProductDetailUiState.Success) {
                ProductDetail(
                    product = (state as ProductDetailUiState.Success).product,
                    addToCart = viewModel::addToCart,
                )
            }
        }

        AnimatedVisibility(
            visible = state is ProductDetailUiState.Error,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (state is ProductDetailUiState.Error) {
                ProductErrorScreen(
                    message = (state as ProductDetailUiState.Error).message,
                )
            }
        }
    }
}

@Composable
fun ProductDetail(
    product: Product,
    addToCart: (product: Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFavorite by remember { mutableStateOf(product.isFavourite ?: false) }
    var quantity by remember { mutableIntStateOf(1) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        // Изображение товара
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(TeaCream),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.image)
                    .crossfade(true)
                    .build(),
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp)),
            )
            
            // Кнопка избранного
            IconButton(
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color.White, CircleShape),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Избранное",
                    tint = if (isFavorite) Color(0xFFE57373) else Color.Gray,
                    modifier = Modifier.size(24.dp),
                )
            }
            
            // Бейдж категории
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = TeaGreen,
            ) {
                Text(
                    text = product.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        
        // Информация о товаре
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                // Название и рейтинг
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3436),
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Рейтинг
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TeaGold.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = TeaGold,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", product.rating),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TeaBrown,
                                ),
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Наличие
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = if (product.count > 10) TeaGreen else Color(0xFFE57373),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (product.count > 10) "В наличии: ${product.count} шт." 
                               else "Осталось мало: ${product.count} шт.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (product.count > 10) TeaGreen else Color(0xFFE57373),
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Описание
                Text(
                    text = "Описание",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D3436),
                    ),
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF636E72),
                        lineHeight = 24.sp,
                    ),
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Нижняя панель с ценой и кнопкой
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Цена и количество
                    Column {
                        Text(
                            text = "Цена",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFB2BEC3),
                            ),
                        )
                        Text(
                            text = "${(product.price * quantity).toInt()} ₽",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TeaGreen,
                            ),
                        )
                    }
                    
                    // Выбор количества
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF5F5F5),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp),
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (quantity > 1) TeaGreen else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Уменьшить",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            
                            Text(
                                text = quantity.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D3436),
                                ),
                            )
                            
                            IconButton(
                                onClick = { if (quantity < product.count) quantity++ },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = TeaGreen,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Увеличить",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Кнопка добавления в корзину
                Button(
                    onClick = { addToCart(product) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TeaGreen,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Добавить в корзину",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProductLoadingScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .rotate(rotation),
                shape = CircleShape,
                color = TeaGreen.copy(alpha = 0.1f),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalCafe,
                        contentDescription = null,
                        tint = TeaGreen,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Загружаем информацию...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TeaBrown,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
fun ProductErrorScreen(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFFFFEBEE),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "☕",
                        fontSize = 36.sp,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Товар не найден",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                ),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF7F8C8D),
                ),
            )
        }
    }
}
