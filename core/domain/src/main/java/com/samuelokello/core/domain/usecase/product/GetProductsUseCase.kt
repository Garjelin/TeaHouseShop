package com.samuelokello.core.domain.usecase.product

import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case для получения списка всех товаров
 * 
 * Инкапсулирует бизнес-логику получения товаров из репозитория
 */
class GetProductsUseCase(
    private val repository: ProductRepository
) {
    /**
     * Выполняет получение списка товаров
     * 
     * @return Flow с результатом операции (Loading, Success, Error)
     */
    operator fun invoke(): Flow<List<Product>> {
        return repository.getProducts()
    }
}
