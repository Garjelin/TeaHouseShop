package com.samuelokello.datasource.local.db.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samuelokello.datasource.local.entity.product.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE category = :category")
    suspend fun getProductsByCategory(category: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE title LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductEntity>

    // sort products by price
    @Query("SELECT * FROM products ORDER BY price ASC")
    suspend fun getProductsByPriceAsc(): List<ProductEntity>

    // pagination and search
    @Query("SELECT * FROM products LIMIT :limit OFFSET :offset")
    suspend fun getProductsByPage(
        limit: Int,
        offset: Int,
    ): List<ProductEntity>

    @Query("SELECT * FROM products ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getProductsPaged(
        limit: Int,
        offset: Int,
    ): List<ProductEntity>

    @Query(
        "SELECT * FROM products WHERE category = :category ORDER BY id ASC LIMIT :limit OFFSET :offset",
    )
    suspend fun getProductsPagedByCategory(
        category: String,
        limit: Int,
        offset: Int,
    ): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countAllProducts(): Int

    @Query("SELECT COUNT(*) FROM products WHERE category = :category")
    suspend fun countProductsInCategory(category: String): Int

    @Query("SELECT DISTINCT category FROM products ORDER BY category COLLATE NOCASE ASC")
    fun observeCategoryLabels(): Flow<List<CategoryLabel>>
}