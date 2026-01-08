package com.samuelokello.data.repository

import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.domain.repository.ProductRepository
import com.samuelokello.remote.sources.product.ProductRemoteSource
import kotlinx.coroutines.flow.Flow

class ProductRepositoryImpl(
    private val remote: ProductRemoteSource,
) : ProductRepository {
    override fun getProducts(): Flow<List<Product>> = remote.getProducts()

    override fun searchProductsWithFilters(
        query: String,
        minPrice: Double?,
        maxPrice: Double?,
        category: String?,
        minCount: Int?,
        minRating: Double?,
    ): Flow<List<Product>> = remote.searchProductsWithFilters(
        query = query,
        minPrice = minPrice,
        maxPrice = maxPrice,
        category = category,
        minCount = minCount,
        minRating = minRating
    )

    override fun getProductById(id: Int): Product = remote.getProductById(id)
}