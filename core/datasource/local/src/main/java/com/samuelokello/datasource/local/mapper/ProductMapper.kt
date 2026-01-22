package com.samuelokello.datasource.local.mapper

import com.samuelokello.core.domain.model.Product
import com.samuelokello.datasource.local.entity.product.ProductEntity

/**
 * Маппер для конвертации между ProductEntity (Database) и Product (Domain)
 */

/**
 * Конвертирует ProductEntity в Domain модель Product
 */
fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        price = price,
        description = description,
        category = category,
        image = thumbnail ?: "", // используем thumbnail как image
        rating = rating,
        count = stock, // используем stock как count
        isFavourite = false // по умолчанию не в избранном, это будет управляться отдельно
    )
}

/**
 * Конвертирует Domain модель Product в ProductEntity
 */
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = 0.0, // значения по умолчанию
        rating = rating,
        stock = count,
        brand = "", // можно добавить в Product если нужно
        sku = "SKU-$id",
        weight = 0.0,
        warrantyInformation = null,
        shippingInformation = null,
        availabilityStatus = if (count > 0) "In Stock" else "Out of Stock",
        returnPolicy = null,
        minimumOrderQuantity = 1,
        thumbnail = image
    )
}

/**
 * Конвертирует список ProductEntity в список Product
 */
fun List<ProductEntity>.toDomain(): List<Product> {
    return map { it.toDomain() }
}

/**
 * Конвертирует список Product в список ProductEntity
 */
fun List<Product>.toEntity(): List<ProductEntity> {
    return map { it.toEntity() }
}
