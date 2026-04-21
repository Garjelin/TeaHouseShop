# Отчёт о выполненных работах по проекту «Tea House Shop»

**Спринт:** #004 (часть 1 — первая половина)  
**Версия:** 0.4.0-alpha  
**Дата:** 10 апреля 2026 г.  
**Статус:** В процессе (50% спринта)

**Исполнитель:** Якимов С.А. (самозанятый)  
**Заказчик:** Заказчик, акцептовавший публичную оферту от 08.01.2026

**Связанный документ:** план работ — `docs/DEVELOPMENT_PLAN.md` (раздел «Спринт 4»).

---

## Цели первой половины спринта 4

Согласно плану, в фокусе:

1. Регистрация и вход в систему (в т.ч. offline-first до появления готового API).
2. Сохранение сессии пользователя между запусками.
3. Валидация форм (email, пароль).

Отложено на **вторую половину** спринта 4: восстановление пароля, экран профиля с просмотром данных и выходом, углублённая интеграция с JWT/сетевым слоем при появлении backend.

---

## Выполненные задачи

### 1. Доменная модель и ошибки

**Новый файл:** `core/domain/.../model/UserProfile.kt`  
Модель профиля: `id`, `email`, `displayName`.

**Обновлён:** `core/domain/.../util/DataError.kt`  
Добавлен перечень `DataError.Auth`: `EMAIL_ALREADY_EXISTS`, `INVALID_CREDENTIALS`, `WEAK_PASSWORD`, `INVALID_EMAIL`, `UNKNOWN`.

**Новый файл:** `core/domain/.../repository/LocalUserAccountRepository.kt`  
Контракт: локальная регистрация и вход, выход, наблюдение за текущим пользователем (`Flow`).

---

### 2. Слой данных: Room, миграция, сессия

**Новый файл:** `core/datasource/local/.../entity/user/LocalUserAccountEntity.kt`  
Таблица `local_user_accounts`: email (уникальный индекс), имя, хеш пароля, соль.

**Новый файл:** `core/datasource/local/.../db/user/LocalUserAccountDao.kt`  
Вставка, выборка по email и по id.

**Новый файл:** `core/datasource/local/.../db/ShopSpotDatabaseMigrations.kt`  
Миграция **1 → 2**: создание таблицы `local_user_accounts` и уникального индекса по email.

**Обновлён:** `core/datasource/local/.../db/ShopSpotDB.kt`  
Версия БД **2**, подключена сущность и DAO `localUserAccountDao()`.

**Обновлён:** `core/datasource/local/.../di/LocalDataSourceModule.kt`  
Регистрация DAO, миграции `addMigrations(MIGRATION_1_2)`, источника `LocalUserAccountSourceImpl` (сессия через **локальный** `PreferenceHelper` / `LocalQualifier`).

**Новый файл:** `core/datasource/local/.../source/user/LocalUserAccountSource.kt`  
Операции с учётными записями и ключом сессии.

**Новый файл:** `core/datasource/local/.../util/PasswordHasher.kt`  
Хеширование SHA-256 с солью (SecureRandom), проверка пароля.

**Обновлён:** `core/datasource/local/.../preference/PreferenceKeys.kt`  
Ключ `CURRENT_LOCAL_USER_ID` — идентификатор текущего пользователя в DataStore (локальные preferences).

---

### 3. Репозиторий и маппинг

**Новый файл:** `core/data/.../mapper/LocalUserAccountMapper.kt`  
`LocalUserAccountEntity` → `UserProfile`.

**Новый файл:** `core/data/.../repository/LocalUserAccountRepositoryImpl.kt`  
Реализация: регистрация (проверка уникальности email, хеш, автоматический вход), вход, выход, `observeCurrentUser()` через комбинацию потока id сессии и чтения из БД.

**Обновлён:** `core/data/.../di/DataModule.kt`  
`single<LocalUserAccountRepository>`, фабрики use cases авторизации.

---

### 4. Use cases

**Новые файлы** в `core/domain/usecase/auth/`:

| Файл | Назначение |
|------|------------|
| `LoginUseCase.kt` | Вход по email и паролю |
| `RegisterUseCase.kt` | Регистрация и автоматический вход |
| `LogoutUseCase.kt` | Очистка сессии (для профиля во 2-й половине) |
| `GetCurrentUserUseCase.kt` | Текущий пользователь как `Flow` (для профиля во 2-й половине) |

---

### 5. Модуль `feat:auth` (UI и ViewModel)

**Обновлён:** `feat/auth/.../login/LoginViewModel.kt`  
Переход с сетевого `AuthenticationRepository` на **`LoginUseCase`**, состояние с полем **email**, обработка `Result` из domain, русскоязычные сообщения об ошибках.

**Новый файл:** `feat/auth/.../register/RegisterViewModel.kt`  
Состояние формы (имя, email, пароль, подтверждение), валидация, навигация после успеха.

**Новые файлы:** `AuthValidation.kt`, `AuthErrorMessages.kt`  
Проверка email и силы пароля, отображение `DataError.Auth` пользователю.

**Обновлён:** `feat/auth/.../login/LoginScreen.kt`  
Поле email, подписи на русском, события `LoginEvent` под новую модель.

**Обновлён:** `feat/auth/.../register/RegisterScreen.kt`  
Связка с `RegisterViewModel`, полноценные поля и ошибки.

**Обновлён:** `feat/auth/.../di/AuthModule.kt`  
`RegisterViewModel` в Koin.

**Обновлён:** `core/presentation/ui/.../ShopSpotAppNavHost.kt`  
У `RegisterScreen` добавлен колбэк `navigateToHome`.

---

### 6. Версия приложения и план

**Обновлён:** `gradle/libs.versions.toml`  
`projectVersionName = "0.4.0-alpha"`, `projectVersionCode = 4`.

**Обновлён:** `docs/DEVELOPMENT_PLAN.md`  
Спринт 4: статус 50%, отмечены выполненные задачи первой половины, зафиксирован offline-first подход и отложенный JWT.

---

## Сводка по файлам

| Категория | Файлы (основные) |
|-----------|------------------|
| Domain | `UserProfile.kt`, `LocalUserAccountRepository.kt`, `DataError.kt`, use cases в `usecase/auth/` |
| Local | `LocalUserAccountEntity`, `LocalUserAccountDao`, миграции, `PasswordHasher`, `LocalUserAccountSource`, `PreferenceKeys`, `ShopSpotDB`, `LocalDataSourceModule` |
| Data | `LocalUserAccountRepositoryImpl`, `LocalUserAccountMapper`, `DataModule` |
| feat/auth | `LoginViewModel`, `LoginScreen`, `RegisterViewModel`, `RegisterScreen`, `AuthModule`, вспомогательные `Auth*` |
| Navigation | `ShopSpotAppNavHost.kt` |
| План / версия | `DEVELOPMENT_PLAN.md`, `libs.versions.toml` |

---

## Прогресс спринта 4 (ориентир)

| Задача | Статус (часть 1) |
|--------|------------------|
| Экран регистрации | Выполнено (локально) |
| Экран входа | Выполнено (локально) |
| JWT/OAuth | Не начато (сохранён существующий `AuthenticationRepository` для будущего API) |
| Сохранение сессии | Выполнено (ID пользователя в DataStore) |
| Восстановление пароля | Запланировано на часть 2 |
| Валидация форм | Выполнено |
| Экран профиля | Запланировано на часть 2 |

---

## Известные ограничения

1. **Сеть:** вход и регистрация работают через локальную БД; сетевой логин из `AuthenticationRemoteSource` не объединён с новым потоком — интеграция при появлении API.
2. **Безопасность:** хеш SHA-256 с солью пригоден для учебного/демо-приложения; для продакшена рассматривают специализированные KDF (например, bcrypt/Argon2 на backend).
3. **`LogoutUseCase` / `GetCurrentUserUseCase`** зарегистрированы в DI, основной UI профиля подключит их во второй половине.

---

## Следующие шаги (часть 2 спринта 4)

- Экран «Забыли пароль» (локальная логика или согласование с API).
- Профиль: отображение `UserProfile`, выход через `LogoutUseCase`.
- При необходимости — навигация «Войти» с основных экранов и согласование UX гостя / авторизованного пользователя.

---

## Заключение

Первая половина спринта 4 закрывает базовую **локальную** авторизацию с **персистентной сессией** и **валидацией форм**, без блокировки на отсутствие backend. Архитектура остаётся совместимой с дальнейшим подключением JWT и существующего репозитория удалённой аутентификации.

---

**Дата составления отчёта:** 10 апреля 2026 г.  
**Подпись:** Якимов С.А.

**Контакт исполнителя:** sergeyyakimov89@gmail.com
