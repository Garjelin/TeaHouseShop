package com.samuelokello.core.domain.usecase.product

import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.repository.ProductRepository

/**
 * Поиск товаров по подстроке в названии (локальная БД).
 */
class SearchProductsUseCase(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(query: String): List<Product> =
        repository.searchProductsByTitle(query.trim())
}
