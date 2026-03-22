package com.samuelokello.datasource.local.db.product

import androidx.room.ColumnInfo

/**
 * Строка результата для SELECT DISTINCT category (Room не мапит напрямую в List<String>).
 */
data class CategoryLabel(
    @ColumnInfo(name = "category") val category: String,
)
