package com.samuelokello.core.domain.usecase.product

import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.model.ProductSearchQuery
import com.samuelokello.core.domain.model.ProductSortOrder
import com.samuelokello.core.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchProductsWithFiltersUseCase(
    private val repository: ProductRepository,
) {
    operator fun invoke(searchQuery: ProductSearchQuery): Flow<List<Product>> =
        repository
            .searchProductsWithFilters(
                query = searchQuery.query.trim(),
                minPrice = searchQuery.minPrice,
                maxPrice = searchQuery.maxPrice,
                category = searchQuery.category,
                minCount = null,
                minRating = searchQuery.minRating,
            ).map { products -> sort(products, searchQuery.sortOrder) }

    private fun sort(
        products: List<Product>,
        sortOrder: ProductSortOrder,
    ): List<Product> =
        when (sortOrder) {
            ProductSortOrder.RELEVANCE -> products
            ProductSortOrder.PRICE_ASC -> products.sortedBy { it.price }
            ProductSortOrder.PRICE_DESC -> products.sortedByDescending { it.price }
            ProductSortOrder.RATING_DESC -> products.sortedByDescending { it.rating }
            ProductSortOrder.TITLE_ASC -> products.sortedBy { it.title.lowercase() }
        }
}
