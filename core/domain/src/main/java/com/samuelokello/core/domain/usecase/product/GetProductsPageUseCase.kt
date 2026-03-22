package com.samuelokello.core.domain.usecase.product

import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.repository.ProductRepository

class GetProductsPageUseCase(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(
        offset: Int,
        limit: Int,
        category: String?,
    ): List<Product> = repository.getProductsPage(offset, limit, category)
}
