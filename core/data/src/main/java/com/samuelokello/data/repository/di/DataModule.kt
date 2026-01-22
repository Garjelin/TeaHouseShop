package com.samuelokello.data.repository.di

import com.samuelokello.core.domain.repository.AuthenticationRepository
import com.samuelokello.core.domain.repository.ProductRepository
import com.samuelokello.core.domain.usecase.product.GetProductByIdUseCase
import com.samuelokello.core.domain.usecase.product.GetProductsUseCase
import com.samuelokello.data.repository.ProductRepositoryImpl
import com.samuelokello.data.repository.repository.AuthenticationRepositoryImpl
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

        // Use Cases
        factory { GetProductsUseCase(get()) }
        factory { GetProductByIdUseCase(get()) }
    }