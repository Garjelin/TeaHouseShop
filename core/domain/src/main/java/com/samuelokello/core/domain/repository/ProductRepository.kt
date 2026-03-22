package com.samuelokello.core.domain.repository

import com.samuelokello.core.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>

    fun observeCategories(): Flow<List<String>>

    suspend fun getProductsPage(
        offset: Int,
        limit: Int,
        category: String?,
    ): List<Product>

    suspend fun countProducts(category: String?): Int

    suspend fun searchProductsByTitle(query: String): List<Product>

    fun searchProductsWithFilters(
        query: String,
        minPrice: Double?,
        maxPrice: Double?,
        category: String?,
        minCount: Int?,
        minRating: Double?,
    ): Flow<List<Product>>

    suspend fun getProductById(id: Int): Product?
}