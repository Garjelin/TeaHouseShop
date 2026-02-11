package com.samuelokello.core.domain.usecase.product

import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.repository.ProductRepository

/**
 * Use Case для получения товара по ID
 * 
 * Инкапсулирует бизнес-логику получения конкретного товара
 */
class GetProductByIdUseCase(
    private val repository: ProductRepository
) {
    /**
     * Выполняет получение товара по ID
     * 
     * @param id идентификатор товара
     * @return товар или null если не найден
     */
    suspend operator fun invoke(id: Int): Product? {
        return repository.getProductById(id)
    }
}
