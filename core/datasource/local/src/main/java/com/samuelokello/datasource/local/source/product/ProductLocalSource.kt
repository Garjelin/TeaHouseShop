package com.samuelokello.datasource.local.source.product

import com.samuelokello.core.domain.model.Product
import com.samuelokello.datasource.local.db.product.ProductDao
import com.samuelokello.datasource.local.entity.product.ProductEntity
import com.samuelokello.datasource.local.mapper.toDomain
import com.samuelokello.datasource.local.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProductLocalSource {
    fun getProducts(): Flow<List<Product>>

    fun observeCategories(): Flow<List<String>>

    suspend fun getProductsPage(
        offset: Int,
        limit: Int,
        category: String?,
    ): List<Product>

    suspend fun countProducts(category: String?): Int

    suspend fun searchProductsByTitle(query: String): List<Product>

    suspend fun insertProducts(products: List<Product>)
    
    suspend fun insertProduct(product: Product)

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

class ProductLocalSourceImpl(
    private val dao: ProductDao,
) : ProductLocalSource {
    
    override fun getProducts(): Flow<List<Product>> {
        return dao.getAllProducts().map { entities ->
            entities.toDomain()
        }
    }

    override fun observeCategories(): Flow<List<String>> {
        return dao.observeCategoryLabels().map { rows -> rows.map { it.category } }
    }

    override suspend fun getProductsPage(
        offset: Int,
        limit: Int,
        category: String?,
    ): List<Product> {
        val entities =
            if (category.isNullOrBlank()) {
                dao.getProductsPaged(limit, offset)
            } else {
                dao.getProductsPagedByCategory(category, limit, offset)
            }
        return entities.toDomain()
    }

    override suspend fun countProducts(category: String?): Int {
        return if (category.isNullOrBlank()) {
            dao.countAllProducts()
        } else {
            dao.countProductsInCategory(category)
        }
    }

    override suspend fun searchProductsByTitle(query: String): List<Product> {
        if (query.isBlank()) return emptyList()
        return dao.searchProducts(query).toDomain()
    }

    override suspend fun insertProducts(products: List<Product>) {
        dao.insertProducts(products.toEntity())
    }
    
    override suspend fun insertProduct(product: Product) {
        dao.insertProducts(product.toEntity())
    }

    override fun searchProductsWithFilters(
        query: String,
        minPrice: Double?,
        maxPrice: Double?,
        category: String?,
        minCount: Int?,
        minRating: Double?,
    ): Flow<List<Product>> {
        // TODO: Реализовать позже с более сложными запросами
        TODO("Not yet implemented - будет реализовано в спринте 7")
    }

    override suspend fun getProductById(id: Int): Product? {
        return dao.getProductById(id)?.toDomain()
    }
}
