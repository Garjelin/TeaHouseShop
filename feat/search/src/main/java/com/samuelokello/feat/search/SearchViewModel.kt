package com.samuelokello.feat.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.model.ProductSearchQuery
import com.samuelokello.core.domain.model.ProductSortOrder
import com.samuelokello.core.domain.usecase.product.GetCategoriesUseCase
import com.samuelokello.core.domain.usecase.product.SearchProductsUseCase
import com.samuelokello.core.domain.usecase.product.SearchProductsWithFiltersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchProductsWithFiltersUseCase: SearchProductsWithFiltersUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchScreenUiState())
    val uiState: StateFlow<SearchScreenUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var suggestionsJob: Job? = null
    private var resultsCollectJob: Job? = null

    init {
        viewModelScope.launch {
            getCategoriesUseCase()
                .catch { }
                .collect { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
        }
        // Стартовый каталог без строки поиска (с фильтрами)
        runSearch(immediate = true)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        updateSuggestions(query)
        runSearch(immediate = false)
    }

    fun onSearchSubmit() {
        val query = _uiState.value.query.trim()
        if (query.isNotBlank()) {
            addToHistory(query)
        }
        runSearch(immediate = true)
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        runSearch(immediate = true)
    }

    fun onMinRatingSelected(minRating: Double?) {
        _uiState.update { it.copy(minRating = minRating) }
        runSearch(immediate = true)
    }

    fun onSortSelected(sortOrder: ProductSortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
        runSearch(immediate = true)
    }

    fun onMinPriceChange(raw: String) {
        val cleaned = raw.filter { it.isDigit() || it == '.' || it == ',' }
        _uiState.update {
            it.copy(
                minPriceText = cleaned,
                minPrice = cleaned.replace(',', '.').toDoubleOrNull(),
            )
        }
        runSearch(immediate = false)
    }

    fun onMaxPriceChange(raw: String) {
        val cleaned = raw.filter { it.isDigit() || it == '.' || it == ',' }
        _uiState.update {
            it.copy(
                maxPriceText = cleaned,
                maxPrice = cleaned.replace(',', '.').toDoubleOrNull(),
            )
        }
        runSearch(immediate = false)
    }

    fun onSuggestionClick(title: String) {
        _uiState.update { it.copy(query = title, suggestions = emptyList()) }
        addToHistory(title)
        runSearch(immediate = true)
    }

    fun onHistoryClick(query: String) {
        _uiState.update { it.copy(query = query) }
        runSearch(immediate = true)
    }

    fun removeFromHistory(query: String) {
        _uiState.update { state ->
            state.copy(recentSearches = state.recentSearches.filter { it != query })
        }
    }

    private fun addToHistory(query: String) {
        _uiState.update { state ->
            state.copy(
                recentSearches = (listOf(query) + state.recentSearches).distinct().take(8),
            )
        }
    }

    private fun updateSuggestions(query: String) {
        suggestionsJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }
        suggestionsJob =
            viewModelScope.launch {
                delay(SUGGESTIONS_DEBOUNCE_MS)
                val titles =
                    runCatching { searchProductsUseCase(query) }
                        .getOrDefault(emptyList())
                        .map { it.title }
                        .distinct()
                        .take(5)
                _uiState.update { it.copy(suggestions = titles) }
            }
    }

    private fun runSearch(immediate: Boolean) {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                if (!immediate) {
                    delay(SEARCH_DEBOUNCE_MS)
                }
                val state = _uiState.value
                _uiState.update { it.copy(isLoading = true, error = null) }

                val searchQuery =
                    ProductSearchQuery(
                        query = state.query,
                        category = state.selectedCategory,
                        minPrice = state.minPrice,
                        maxPrice = state.maxPrice,
                        minRating = state.minRating,
                        sortOrder = state.sortOrder,
                    )

                resultsCollectJob?.cancel()
                resultsCollectJob =
                    launch {
                        searchProductsWithFiltersUseCase(searchQuery)
                            .catch { e ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = e.message ?: "Ошибка поиска",
                                    )
                                }
                            }.collect { products ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        products = products,
                                        error = null,
                                    )
                                }
                            }
                    }
            }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 350L
        private const val SUGGESTIONS_DEBOUNCE_MS = 250L
    }
}

data class SearchScreenUiState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val minRating: Double? = null,
    val sortOrder: ProductSortOrder = ProductSortOrder.RELEVANCE,
    val minPriceText: String = "",
    val maxPriceText: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val suggestions: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
