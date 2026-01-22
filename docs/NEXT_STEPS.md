# Следующие шаги разработки

> **Текущая версия:** 0.1.1-alpha (в разработке)  
> **Дата:** 22 января 2026 г.  
> **Прогресс Спринта 2:** 70%

---

## 🎯 Что сделано на данный момент

✅ Room Database полностью интегрирована  
✅ Маппинг Entity ↔ Domain работает  
✅ Use Cases созданы (GetProducts, GetProductById)  
✅ Mock-данные инициализируются автоматически (12 товаров чая)  
✅ Dependency Injection настроен  
✅ План разработки скорректирован под автономную работу  

---

## 🚀 Что нужно сделать дальше (в порядке приоритета)

### 1. Проверить работоспособность 🔍

Сейчас изменения сделаны, но не проверены. Необходимо:

```bash
# Собрать проект
./gradlew clean build

# Запустить на эмуляторе/устройстве
./gradlew installDebug
```

**Ожидаемый результат:**
- Приложение запускается
- При первом запуске БД инициализируется 12 товарами
- Товары отображаются на главном экране

**Что проверить:**
- [ ] Приложение компилируется без ошибок
- [ ] Нет краш-ов при запуске
- [ ] Mock-данные появляются в БД (проверить через Database Inspector в Android Studio)

---

### 2. Интегрировать Use Cases в HomeViewModel 🔗

**Файл:** `feat/home/src/main/java/com/samuelokello/feat/home/HomeViewModel.kt`

**Текущее состояние:**
```kotlin
// HomeViewModel сейчас использует старый способ получения данных
```

**Что нужно сделать:**
```kotlin
class HomeViewModel(
    private val getProductsUseCase: GetProductsUseCase // добавить через Koin
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadProducts()
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase()
                .catch { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = error.message) 
                    }
                }
                .collect { products ->
                    _uiState.update { 
                        it.copy(products = products, isLoading = false) 
                    }
                }
        }
    }
}
```

**Не забыть:**
- Зарегистрировать HomeViewModel в Koin с зависимостью GetProductsUseCase
- Обновить UI состояния (HomeUiState)

---

### 3. Добавить обработку ошибок ⚠️

**Создать:** `core/domain/util/Result.kt` или использовать существующий `Resource.kt`

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
```

**Обновить Use Cases:**
```kotlin
class GetProductsUseCase(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            repository.getProducts().collect { products ->
                emit(Resource.Success(products))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error", e))
        }
    }
}
```

---

### 4. Написать базовые тесты 🧪

#### Unit-тест для маппера

**Создать:** `core/datasource/local/src/test/java/mapper/ProductMapperTest.kt`

```kotlin
class ProductMapperTest {
    @Test
    fun `ProductEntity toDomain maps correctly`() {
        val entity = ProductEntity(
            id = 1,
            title = "Test Tea",
            price = 100.0,
            // ... остальные поля
        )
        
        val domain = entity.toDomain()
        
        assertEquals(1, domain.id)
        assertEquals("Test Tea", domain.title)
        assertEquals(100.0, domain.price, 0.01)
    }
    
    @Test
    fun `Product toEntity maps correctly`() {
        // аналогично
    }
}
```

#### Unit-тест для Use Case

**Создать:** `core/domain/src/test/java/usecase/GetProductsUseCaseTest.kt`

```kotlin
class GetProductsUseCaseTest {
    private lateinit var useCase: GetProductsUseCase
    private lateinit var repository: ProductRepository
    
    @Before
    fun setup() {
        repository = mockk()
        useCase = GetProductsUseCase(repository)
    }
    
    @Test
    fun `invoke returns products from repository`() = runTest {
        // Given
        val products = listOf(
            Product(id = 1, title = "Tea", ...)
        )
        every { repository.getProducts() } returns flowOf(products)
        
        // When
        val result = useCase().first()
        
        // Then
        assertEquals(products, result)
    }
}
```

---

### 5. Проверить инициализацию данных 📦

**Использовать Database Inspector в Android Studio:**

1. Запустить приложение
2. Tools → Database Inspector
3. Открыть `shopspot.db` → таблица `products`
4. Убедиться, что есть 12 записей

**Альтернатива - логирование:**

```kotlin
// В MockDataInitializer
suspend fun initializeIfNeeded() {
    val existingProducts = productLocalSource.getProducts().first()
    
    Log.d("MockDataInit", "Existing products: ${existingProducts.size}")
    
    if (existingProducts.isEmpty()) {
        Log.d("MockDataInit", "Initializing mock data...")
        productLocalSource.insertProducts(getMockProducts())
        Log.d("MockDataInit", "Mock data initialized!")
    } else {
        Log.d("MockDataInit", "Data already exists, skipping initialization")
    }
}
```

---

## 📋 Чек-лист перед продолжением

- [ ] Проект компилируется без ошибок
- [ ] Приложение запускается на устройстве/эмуляторе
- [ ] Mock-данные появляются в БД при первом запуске
- [ ] HomeViewModel использует GetProductsUseCase
- [ ] Товары отображаются на главном экране
- [ ] Добавлена базовая обработка ошибок
- [ ] Написаны unit-тесты для маппера
- [ ] Написаны unit-тесты для Use Case

---

## 🎓 Полезные команды

### Gradle

```bash
# Очистить проект
./gradlew clean

# Собрать проект
./gradlew build

# Собрать и установить debug версию
./gradlew installDebug

# Запустить тесты
./gradlew test

# Запустить линтер
./gradlew ktlintCheck

# Автоматически исправить стиль кода
./gradlew ktlintFormat

# Проверить зависимости
./gradlew dependencies
```

### Android Studio

```
Database Inspector: Tools → Database Inspector
Logcat: View → Tool Windows → Logcat
Profiler: View → Tool Windows → Profiler
```

---

## 📚 Полезная документация

- [План разработки](DEVELOPMENT_PLAN.md)
- [Отчёт Спринт 2 (частичный)](reports/REPORT_SPRINT_002_2026-01-23.md)
- [История изменений](CHANGELOG.md)
- [Архитектура модулей](modules.md)
- [Техническое задание](TECHNICAL_REQUIREMENTS.md)

---

## 🆘 Если что-то не работает

### Проблема: Ошибки компиляции

**Решение:**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Проблема: Koin не может найти зависимости

**Решение:**
- Проверить что все модули добавлены в `ShopSpotApp.configureKoin()`
- Проверить что компоненты зарегистрированы в правильных модулях

### Проблема: БД не инициализируется

**Решение:**
- Удалить приложение и установить заново
- Проверить логи: `adb logcat | grep MockDataInit`
- Проверить что MockDataInitializer вызывается в ShopSpotApp

### Проблема: Crash при запуске

**Решение:**
- Проверить Logcat для stack trace
- Убедиться что все Room Entity имеют правильные аннотации
- Проверить что Database создан с правильными entities

---

## 💬 Вопросы?

Якимов Сергей  
Email: sergeyyakimov89@gmail.com  
GitHub: Tea House Shop

---

**Удачи в разработке! 🍵**
