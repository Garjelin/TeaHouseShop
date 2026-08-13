package com.samuelokello.feat.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.model.ProductSortOrder
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
    navigateToItemDetails: (product: Product) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    val activeFiltersCount = uiState.activeFiltersCount()

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Поиск чая...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.query.isNotBlank()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Очистить")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.onSearchSubmit()
                    },
                ),
        )

        if (uiState.suggestions.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                uiState.suggestions.forEach { suggestion ->
                    Text(
                        text = suggestion,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onSuggestionClick(suggestion) }
                                .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        OutlinedButton(
            onClick = { filtersExpanded = !filtersExpanded },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text =
                    when {
                        filtersExpanded -> "Скрыть фильтры"
                        activeFiltersCount > 0 -> "Фильтры ($activeFiltersCount)"
                        else -> "Фильтры и сортировка"
                    },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (filtersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }

        if (filtersExpanded) {
            SearchFiltersPanel(
                uiState = uiState,
                onCategorySelected = viewModel::onCategorySelected,
                onMinRatingSelected = viewModel::onMinRatingSelected,
                onMinPriceChange = viewModel::onMinPriceChange,
                onMaxPriceChange = viewModel::onMaxPriceChange,
                onSortSelected = viewModel::onSortSelected,
            )
        }

        if (uiState.recentSearches.isNotEmpty() && uiState.query.isBlank()) {
            Text(
                text = "Недавние запросы",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            uiState.recentSearches.forEach { item ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onHistoryClick(item) }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = item)
                    IconButton(onClick = { viewModel.removeFromHistory(item) }) {
                        Icon(Icons.Default.Close, contentDescription = "Удалить")
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
            uiState.products.isEmpty() -> {
                Text(
                    text = "Ничего не найдено",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        Text(
                            text = "Найдено: ${uiState.products.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(uiState.products, key = { it.id }) { product ->
                        SearchProductRow(
                            product = product,
                            onClick = { navigateToItemDetails(product) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFiltersPanel(
    uiState: SearchScreenUiState,
    onCategorySelected: (String?) -> Unit,
    onMinRatingSelected: (Double?) -> Unit,
    onMinPriceChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onSortSelected: (ProductSortOrder) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Категория",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Все") },
            )
            uiState.categories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                )
            }
        }

        Text(
            text = "Рейтинг",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = uiState.minRating == null,
                onClick = { onMinRatingSelected(null) },
                label = { Text("Любой") },
            )
            FilterChip(
                selected = uiState.minRating == 3.0,
                onClick = { onMinRatingSelected(3.0) },
                label = { Text("от 3★") },
            )
            FilterChip(
                selected = uiState.minRating == 4.0,
                onClick = { onMinRatingSelected(4.0) },
                label = { Text("от 4★") },
            )
            FilterChip(
                selected = uiState.minRating == 4.5,
                onClick = { onMinRatingSelected(4.5) },
                label = { Text("от 4.5★") },
            )
        }

        Text(
            text = "Цена, ₽",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = uiState.minPriceText,
                onValueChange = onMinPriceChange,
                modifier = Modifier.weight(1f),
                label = { Text("от") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                value = uiState.maxPriceText,
                onValueChange = onMaxPriceChange,
                modifier = Modifier.weight(1f),
                label = { Text("до") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }

        Text(
            text = "Сортировка",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SortChip("По умолчанию", ProductSortOrder.RELEVANCE, uiState.sortOrder, onSortSelected)
            SortChip("Цена ↑", ProductSortOrder.PRICE_ASC, uiState.sortOrder, onSortSelected)
            SortChip("Цена ↓", ProductSortOrder.PRICE_DESC, uiState.sortOrder, onSortSelected)
            SortChip("Рейтинг", ProductSortOrder.RATING_DESC, uiState.sortOrder, onSortSelected)
            SortChip("Название", ProductSortOrder.TITLE_ASC, uiState.sortOrder, onSortSelected)
        }
    }
}

private fun SearchScreenUiState.activeFiltersCount(): Int {
    var count = 0
    if (selectedCategory != null) count++
    if (minRating != null) count++
    if (minPrice != null) count++
    if (maxPrice != null) count++
    if (sortOrder != ProductSortOrder.RELEVANCE) count++
    return count
}

@Composable
private fun SortChip(
    label: String,
    value: ProductSortOrder,
    selected: ProductSortOrder,
    onSelected: (ProductSortOrder) -> Unit,
) {
    FilterChip(
        selected = selected == value,
        onClick = { onSelected(value) },
        label = { Text(label) },
    )
}

@Composable
private fun SearchProductRow(
    product: Product,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = product.image,
            contentDescription = null,
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
            )
            Text(
                text =
                    "${product.category} · ${
                        String.format(Locale("ru", "RU"), "%.1f★", product.rating)
                    } · ${
                        String.format(Locale("ru", "RU"), "%.0f ₽", product.price)
                    }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
