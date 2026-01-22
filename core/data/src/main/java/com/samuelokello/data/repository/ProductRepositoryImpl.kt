package com.samuelokello.data.repository

import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.repository.ProductRepository
import com.samuelokello.datasource.local.source.product.ProductLocalSource
import kotlinx.coroutines.flow.Flow

/**
 * Реализация репозитория товаров
 * 
 * Использует Room Database как единственный источник данных (offline-first подход)
 * В будущем можно добавить синхронизацию с удалённым API
 */
class ProductRepositoryImpl(
    private val localSource: ProductLocalSource,
    // private val remoteSource: ProductRemoteSource, // для будущей интеграции с API
) : ProductRepository {
    
    override fun getProducts(): Flow<List<Product>> {
        // Возвращаем данные из локальной БД
        return localSource.getProducts()
    }

    override fun searchProductsWithFilters(
        query: String,
        minPrice: Double?,
        maxPrice: Double?,
        category: String?,
        minCount: Int?,
        minRating: Double?,
    ): Flow<List<Product>> {
        // TODO: Реализовать в спринте 7
        return localSource.searchProductsWithFilters(
            query = query,
            minPrice = minPrice,
            maxPrice = maxPrice,
            category = category,
            minCount = minCount,
            minRating = minRating
        )
    }

    override fun getProductById(id: Int): Product {
        // TODO: Сделать suspend и вернуть Flow в будущем
        throw NotImplementedError("Будет реализовано в следующем спринте")
    }
}