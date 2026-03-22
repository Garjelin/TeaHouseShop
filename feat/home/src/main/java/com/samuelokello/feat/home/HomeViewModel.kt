package com.samuelokello.feat.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.usecase.product.CountProductsUseCase
import com.samuelokello.core.domain.usecase.product.GetCategoriesUseCase
import com.samuelokello.core.domain.usecase.product.GetProductsPageUseCase
import com.samuelokello.core.domain.usecase.product.SearchProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getProductsPageUseCase: GetProductsPageUseCase,
    private val countProductsUseCase: CountProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
) : ViewModel() {
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private var accumulated: List<Product> = emptyList()
    private var currentOffset = 0
    private var totalCount = 0
    private var selectedCategory: String? = null
    private var searchQuery: String = ""

    init {
        viewModelScope.launch {
            getCategoriesUseCase()
                .catch { /* оставляем предыдущие категории при сбое потока */ }
                .collect { categories ->
                    _homeUiState.update { state ->
                        if (state is HomeUiState.Success) {
                            state.copy(categories = categories)
                        } else {
                            state
                        }
                    }
                }
        }
        viewModelScope.launch {
            loadFirstPage(showFullLoading = true)
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            loadFirstPage(showFullLoading = true)
        }
    }

    fun selectCategory(category: String?) {
        selectedCategory = category
        viewModelScope.launch {
            if (searchQuery.isNotBlank()) {
                applySearchWithQuery(searchQuery)
            } else {
                loadFirstPage(showFullLoading = true)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadFirstPage(showFullLoading = false)
            } else {
                applySearchWithQuery(query)
            }
        }
    }

    fun loadMore() {
        if (searchQuery.isNotBlank()) return
        val state = _homeUiState.value
        if (state !is HomeUiState.Success || !state.hasMore || state.isLoadingMore) return
        viewModelScope.launch {
            _homeUiState.update { (it as HomeUiState.Success).copy(isLoadingMore = true) }
            try {
                val page =
                    getProductsPageUseCase(
                        offset = currentOffset,
                        limit = PAGE_SIZE,
                        category = selectedCategory,
                    )
                accumulated = accumulated + page
                currentOffset += page.size
                _homeUiState.update {
                    (it as HomeUiState.Success).copy(
                        products = accumulated,
                        isLoadingMore = false,
                        hasMore = currentOffset < totalCount,
                    )
                }
            } catch (_: Exception) {
                _homeUiState.update {
                    (it as HomeUiState.Success).copy(isLoadingMore = false)
                }
            }
        }
    }

    private suspend fun loadFirstPage(showFullLoading: Boolean) {
        if (showFullLoading) {
            _homeUiState.value = HomeUiState.Loading
        } else {
            _homeUiState.update { s ->
                if (s is HomeUiState.Success) {
                    s.copy(
                        isLoadingMore = true,
                        products = emptyList(),
                        hasMore = false,
                    )
                } else {
                    s
                }
            }
        }
        try {
            totalCount = countProductsUseCase(selectedCategory)
            val firstPage =
                getProductsPageUseCase(
                    offset = 0,
                    limit = PAGE_SIZE,
                    category = selectedCategory,
                )
            accumulated = firstPage
            currentOffset = firstPage.size
            val categories = getCategoriesUseCase().first()
            _homeUiState.value =
                HomeUiState.Success(
                    products = accumulated,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    isLoadingMore = false,
                    hasMore = currentOffset < totalCount,
                )
        } catch (e: Exception) {
            _homeUiState.value =
                HomeUiState.Error(message = e.message ?: "Unknown error")
        }
    }

    private suspend fun applySearchWithQuery(query: String) {
        val cat = selectedCategory
        var results = searchProductsUseCase(query)
        if (!cat.isNullOrBlank()) {
            results = results.filter { it.category == cat }
        }
        val categories =
            (_homeUiState.value as? HomeUiState.Success)?.categories
                ?: getCategoriesUseCase().first()
        _homeUiState.value =
            HomeUiState.Success(
                products = results,
                categories = categories,
                selectedCategory = selectedCategory,
                searchQuery = query,
                isLoadingMore = false,
                hasMore = false,
            )
    }

    companion object {
        private const val PAGE_SIZE = 4
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Error(
        val message: String,
    ) : HomeUiState

    data class Success(
        val products: List<Product>,
        val categories: List<String>,
        val selectedCategory: String?,
        val searchQuery: String,
        val isLoadingMore: Boolean,
        val hasMore: Boolean,
    ) : HomeUiState
}
