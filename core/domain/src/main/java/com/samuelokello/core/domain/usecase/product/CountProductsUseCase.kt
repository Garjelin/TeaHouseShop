package com.samuelokello.core.domain.usecase.product

import com.samuelokello.core.domain.repository.ProductRepository

class CountProductsUseCase(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(category: String?): Int = repository.countProducts(category)
}
