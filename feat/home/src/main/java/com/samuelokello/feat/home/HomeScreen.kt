package com.samuelokello.feat.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * @Product Screen - Lists all available products
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navigateToItemDetails: (productId: Int) -> Unit,
    onBackPressed: () -> Unit,
) {
    val state by viewModel.homeUiState.collectAsState()

    var backPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > 2000) {
            backPressedTime = currentTime
            Toast.makeText(context, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
        } else {
            onBackPressed()
        }
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
            visible = state is HomeUiState.Error,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (state is HomeUiState.Error) {
                ErrorScreen(
                    message = (state as HomeUiState.Error).message,
                    onRetry = { viewModel.loadProducts() },
                )
            }
        }

        AnimatedVisibility(
            visible = state is HomeUiState.Loading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LoadingScreen()
        }

        AnimatedVisibility(
            visible = state is HomeUiState.Success,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(),
        ) {
            if (state is HomeUiState.Success) {
                val products = (state as HomeUiState.Success).products
                if (products.isEmpty()) {
                    EmptyScreen()
                } else {
                    ProductList(
                        products = products,
                        navigateToItemDetails = navigateToItemDetails,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductList(
    modifier: Modifier = Modifier,
    products: List<Product>,
    navigateToItemDetails: (productId: Int) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        // Заголовок
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalCafe,
                        contentDescription = null,
                        tint = TeaGreen,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tea House",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TeaBrown,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Откройте мир изысканного чая",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF7F8C8D),
                    ),
                )
            }
        }
        
        // Поиск
        item(span = { GridItemSpan(2) }) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                placeholder = {
                    Text(
                        text = "Найти чай...",
                        color = Color(0xFFB2BEC3),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TeaGreen,
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = TeaGreen,
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        }
        
        // Статистика
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Каталог",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D3436),
                    ),
                )
                Text(
                    text = "${filteredProducts.size} товаров",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF7F8C8D),
                    ),
                )
            }
        }

        // Товары
        items(
            items = filteredProducts,
            key = { it.id }
        ) { product ->
            ProductItem(
                product = product,
                navigateToItemDetails = { navigateToItemDetails(product.id) },
            )
        }
        
        // Отступ внизу
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
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
            // Анимированная иконка чая
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
                text = "Завариваем каталог...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TeaBrown,
                    fontWeight = FontWeight.Medium,
                ),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Точки загрузки
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = TeaGreen.copy(alpha = dotAlpha),
                                shape = CircleShape
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
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
            // Иконка ошибки
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
                text = "Упс! Чай остыл",
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                color = TeaGreen,
            ) {
                Text(
                    text = "Попробовать снова",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
fun EmptyScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            // Иконка пустого состояния
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = TeaCream,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalCafe,
                        contentDescription = null,
                        tint = TeaGreenLight,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Каталог пуст",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436),
                ),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Скоро здесь появятся лучшие сорта чая",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF7F8C8D),
                ),
            )
        }
    }
}
