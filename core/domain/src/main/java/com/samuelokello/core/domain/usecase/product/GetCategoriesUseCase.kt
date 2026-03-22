package com.samuelokello.core.domain.usecase.product

import com.samuelokello.core.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(
    private val repository: ProductRepository,
) {
    operator fun invoke(): Flow<List<String>> = repository.observeCategories()
}
