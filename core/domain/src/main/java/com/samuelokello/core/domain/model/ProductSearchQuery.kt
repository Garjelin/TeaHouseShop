package com.samuelokello.core.domain.model

enum class ProductSortOrder {
    RELEVANCE,
    PRICE_ASC,
    PRICE_DESC,
    RATING_DESC,
    TITLE_ASC,
}

data class ProductSearchQuery(
    val query: String = "",
    val category: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null,
    val sortOrder: ProductSortOrder = ProductSortOrder.RELEVANCE,
)
