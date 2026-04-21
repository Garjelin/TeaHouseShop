package com.samuelokello.data.repository.di

import com.samuelokello.core.domain.repository.AuthenticationRepository
import com.samuelokello.core.domain.repository.LocalUserAccountRepository
import com.samuelokello.core.domain.repository.ProductRepository
import com.samuelokello.core.domain.usecase.auth.GetCurrentUserUseCase
import com.samuelokello.core.domain.usecase.auth.LoginUseCase
import com.samuelokello.core.domain.usecase.auth.LogoutUseCase
import com.samuelokello.core.domain.usecase.auth.RegisterUseCase
import com.samuelokello.core.domain.usecase.product.CountProductsUseCase
import com.samuelokello.core.domain.usecase.product.GetCategoriesUseCase
import com.samuelokello.core.domain.usecase.product.GetProductByIdUseCase
import com.samuelokello.core.domain.usecase.product.GetProductsPageUseCase
import com.samuelokello.core.domain.usecase.product.GetProductsUseCase
import com.samuelokello.core.domain.usecase.product.SearchProductsUseCase
import com.samuelokello.data.repository.ProductRepositoryImpl
import com.samuelokello.data.repository.repository.AuthenticationRepositoryImpl
import com.samuelokello.data.repository.repository.LocalUserAccountRepositoryImpl
import org.koin.dsl.module

val dataModule =
    module {
        // Repositories
        single<AuthenticationRepository> {
            AuthenticationRepositoryImpl(
                localSource = get(),
                remoteSource = get(),
            )
        }
        single<ProductRepository> {
            ProductRepositoryImpl(
                localSource = get(),
            )
        }

        single<LocalUserAccountRepository> {
            LocalUserAccountRepositoryImpl(
                source = get(),
            )
        }

        // Use Cases
        factory { GetProductsUseCase(get()) }
        factory { GetProductByIdUseCase(get()) }
        factory { GetCategoriesUseCase(get()) }
        factory { GetProductsPageUseCase(get()) }
        factory { CountProductsUseCase(get()) }
        factory { SearchProductsUseCase(get()) }

        factory { LoginUseCase(get()) }
        factory { RegisterUseCase(get()) }
        factory { LogoutUseCase(get()) }
        factory { GetCurrentUserUseCase(get()) }
    }