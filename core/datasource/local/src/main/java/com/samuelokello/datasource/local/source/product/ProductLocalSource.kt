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
