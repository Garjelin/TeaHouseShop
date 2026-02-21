package com.samuelokello.datasource.local.util

import android.content.Context
import com.samuelokello.core.domain.model.Product
import com.samuelokello.core.presentation.designsystem.R
import com.samuelokello.datasource.local.source.product.ProductLocalSource
import kotlinx.coroutines.flow.first

/**
 * Утилита для инициализации базы данных mock-данными при первом запуске
 */
class MockDataInitializer(
    private val productLocalSource: ProductLocalSource,
    private val context: Context
) {
    
    companion object {
        private const val PREFS_NAME = "mock_data_prefs"
        private const val KEY_DATA_VERSION = "data_version"
        // Увеличивайте эту версию при изменении mock-данных
        private const val CURRENT_DATA_VERSION = 6
    }
    
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Создаёт URI для локального drawable ресурса по его ID
     */
    private fun getDrawableUri(resourceId: Int): String {
        return "android.resource://${context.packageName}/$resourceId"
    }
    
    /**
     * Проверяет наличие данных в БД и инициализирует их при необходимости
     * Также обновляет данные если версия изменилась
     */
    suspend fun initializeIfNeeded() {
        val existingProducts = productLocalSource.getProducts().first()
        val savedVersion = prefs.getInt(KEY_DATA_VERSION, 0)
        
        if (existingProducts.isEmpty() || savedVersion < CURRENT_DATA_VERSION) {
            // Инициализируем или обновляем mock-данные
            productLocalSource.insertProducts(getMockProducts())
            prefs.edit().putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION).apply()
        }
    }
    
    /**
     * Принудительно обновляет все товары
     */
    suspend fun forceUpdate() {
        productLocalSource.insertProducts(getMockProducts())
        prefs.edit().putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION).apply()
    }
    
    /**
     * Возвращает список mock-данных для чайного магазина
     * Используем локальные drawable ресурсы - работают офлайн!
     */
    private fun getMockProducts(): List<Product> {
        return listOf(
            Product(
                id = 1,
                title = "Зелёный чай Лунцзин",
                price = 450.0,
                description = "Премиальный китайский зелёный чай из провинции Чжэцзян. " +
                        "Обладает нежным ореховым ароматом и свежим вкусом. " +
                        "Идеален для утреннего чаепития.",
                category = "Зелёный чай",
                image = getDrawableUri(R.drawable.tea_green),
                rating = 4.8,
                count = 120,
                isFavourite = false
            ),
            Product(
                id = 2,
                title = "Пуэр Шу 2015",
                price = 890.0,
                description = "Выдержанный тёмный пуэр с мягким землистым вкусом. " +
                        "Производство 2015 года. Помогает пищеварению и бодрит.",
                category = "Пуэр",
                image = getDrawableUri(R.drawable.tea_puer),
                rating = 4.9,
                count = 45,
                isFavourite = false
            ),
            Product(
                id = 3,
                title = "Улун Те Гуань Инь",
                price = 650.0,
                description = "Классический китайский улун с цветочным ароматом. " +
                        "Полуферментированный чай с богатым вкусом и долгим послевкусием.",
                category = "Улун",
                image = getDrawableUri(R.drawable.tea_oolong),
                rating = 4.7,
                count = 78,
                isFavourite = false
            ),
            Product(
                id = 4,
                title = "Белый чай Бай Му Дань",
                price = 720.0,
                description = "Нежный белый чай с лёгким сладковатым вкусом. " +
                        "Минимальная обработка сохраняет все полезные свойства.",
                category = "Белый чай",
                image = getDrawableUri(R.drawable.tea_white),
                rating = 4.6,
                count = 32,
                isFavourite = false
            ),
            Product(
                id = 5,
                title = "Да Хун Пао",
                price = 1200.0,
                description = "Легендарный утёсный улун из гор Уи. " +
                        "Один из самых известных и дорогих китайских чаёв. " +
                        "Глубокий вкус с нотками карамели и орехов.",
                category = "Улун",
                image = getDrawableUri(R.drawable.tea_oolong),
                rating = 5.0,
                count = 15,
                isFavourite = false
            ),
            Product(
                id = 6,
                title = "Сенча",
                price = 380.0,
                description = "Японский зелёный чай повседневного употребления. " +
                        "Свежий травянистый вкус, богат антиоксидантами.",
                category = "Зелёный чай",
                image = getDrawableUri(R.drawable.tea_green),
                rating = 4.5,
                count = 95,
                isFavourite = false
            ),
            Product(
                id = 7,
                title = "Матча Церемониальная",
                price = 950.0,
                description = "Высший сорт порошкового зелёного чая для чайной церемонии. " +
                        "Насыщенный вкус и яркий зелёный цвет.",
                category = "Зелёный чай",
                image = getDrawableUri(R.drawable.tea_green),
                rating = 4.9,
                count = 28,
                isFavourite = false
            ),
            Product(
                id = 8,
                title = "Жасминовый чай",
                price = 520.0,
                description = "Зелёный чай с натуральными цветами жасмина. " +
                        "Изысканный цветочный аромат и мягкий вкус.",
                category = "Зелёный чай",
                image = getDrawableUri(R.drawable.tea_green),
                rating = 4.7,
                count = 67,
                isFavourite = false
            ),
            Product(
                id = 9,
                title = "Лапсанг Сушонг",
                price = 580.0,
                description = "Копчёный чёрный чай с насыщенным дымным ароматом. " +
                        "Необычный вкус для любителей экспериментов.",
                category = "Чёрный чай",
                image = getDrawableUri(R.drawable.tea_black),
                rating = 4.3,
                count = 42,
                isFavourite = false
            ),
            Product(
                id = 10,
                title = "Ассам",
                price = 340.0,
                description = "Классический индийский чёрный чай. " +
                        "Крепкий, солодовый вкус. Отлично подходит для завтрака.",
                category = "Чёрный чай",
                image = getDrawableUri(R.drawable.tea_black),
                rating = 4.6,
                count = 110,
                isFavourite = false
            ),
            Product(
                id = 11,
                title = "Серебряные иглы",
                price = 1450.0,
                description = "Элитный белый чай из провинции Фуцзянь. " +
                        "Собирается только из почек. Деликатный и утончённый.",
                category = "Белый чай",
                image = getDrawableUri(R.drawable.tea_white),
                rating = 5.0,
                count = 8,
                isFavourite = false
            ),
            Product(
                id = 12,
                title = "Молочный улун",
                price = 690.0,
                description = "Улун с естественным молочно-сливочным ароматом. " +
                        "Мягкий и нежный вкус, любимец многих.",
                category = "Улун",
                image = getDrawableUri(R.drawable.tea_oolong),
                rating = 4.8,
                count = 56,
                isFavourite = false
            )
        )
    }
}
