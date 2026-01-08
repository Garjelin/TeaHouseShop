package com.samuelokello.remote.sources.product

import com.samuelokello.core.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ProductRemoteSource {
    fun getProducts(): Flow<List<Product>>

    fun searchProductsWithFilters(
        query: String,
        minPrice: Double?,
        maxPrice: Double?,
        category: String?,
        minCount: Int?,
        minRating: Double?,
    ): Flow<List<Product>>

    fun getProductById(id: Int): Product
}

class ProductRemoteSourceImpl : ProductRemoteSource {
    // Временные тестовые данные
    private val mockProducts = listOf(
        Product(
            id = 1,
            title = "Зелёный чай Лунцзин",
            price = 450.0,
            description = "Премиальный китайский зелёный чай из провинции Чжэцзян",
            category = "Зелёный чай",
            image = "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=400",
            rating = 4.8,
            count = 120,
            isFavourite = false
        ),
        Product(
            id = 2,
            title = "Пуэр Шу 2015",
            price = 890.0,
            description = "Выдержанный тёмный пуэр с мягким землистым вкусом",
            category = "Пуэр",
            image = "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400",
            rating = 4.9,
            count = 45,
            isFavourite = true
        ),
        Product(
            id = 3,
            title = "Улун Те Гуань Инь",
            price = 650.0,
            description = "Классический китайский улун с цветочным ароматом",
            category = "Улун",
            image = "https://images.unsplash.com/photo-1558160074-4d7d8bdf4256?w=400",
            rating = 4.7,
            count = 78,
            isFavourite = false
        ),
        Product(
            id = 4,
            title = "Белый чай Бай Му Дань",
            price = 720.0,
            description = "Нежный белый чай с лёгким сладковатым вкусом",
            category = "Белый чай",
            image = "https://images.unsplash.com/photo-1597318181409-cf64d0b5d8a2?w=400",
            rating = 4.6,
            count = 32,
            isFavourite = false
        ),
        Product(
            id = 5,
            title = "Да Хун Пао",
            price = 1200.0,
            description = "Легендарный утёсный улун из гор Уи",
            category = "Улун",
            image = "https://images.unsplash.com/photo-1571934811356-5cc061b6821f?w=400",
            rating = 5.0,
            count = 15,
            isFavourite = true
        )
    )

    override fun getProducts(): Flow<List<Product>> = flowOf(mockProducts)

    override fun searchProductsWithFilters(
        query: String,
        minPrice: Double?,
        maxPrice: Double?,
        category: String?,
        minCount: Int?,
        minRating: Double?,
    ): Flow<List<Product>> = flowOf(
        mockProducts.filter { product ->
            (query.isBlank() || product.title.contains(query, ignoreCase = true)) &&
            (minPrice == null || product.price >= minPrice) &&
            (maxPrice == null || product.price <= maxPrice) &&
            (category == null || product.category == category) &&
            (minCount == null || product.count >= minCount) &&
            (minRating == null || product.rating >= minRating)
        }
    )

    override fun getProductById(id: Int): Product =
        mockProducts.find { it.id == id } ?: throw NoSuchElementException("Product not found")
}