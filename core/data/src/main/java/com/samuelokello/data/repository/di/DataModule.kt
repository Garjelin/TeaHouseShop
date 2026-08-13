package com.samuelokello.data.repository.di

import com.samuelokello.core.domain.repository.AuthenticationRepository
import com.samuelokello.core.domain.repository.CartRepository
import com.samuelokello.core.domain.repository.LocalUserAccountRepository
import com.samuelokello.core.domain.repository.OrderRepository
import com.samuelokello.core.domain.repository.ProductRepository
import com.samuelokello.core.domain.usecase.auth.GetCurrentUserUseCase
import com.samuelokello.core.domain.usecase.auth.LoginUseCase
import com.samuelokello.core.domain.usecase.auth.LogoutUseCase
import com.samuelokello.core.domain.usecase.auth.RegisterUseCase
import com.samuelokello.core.domain.usecase.auth.ResetPasswordUseCase
import com.samuelokello.core.domain.usecase.cart.AddToCartUseCase
import com.samuelokello.core.domain.usecase.cart.ClearCartUseCase
import com.samuelokello.core.domain.usecase.order.CreateOrderUseCase
import com.samuelokello.core.domain.usecase.order.GetOrderByIdUseCase
import com.samuelokello.core.domain.usecase.order.GetUserOrdersUseCase
import com.samuelokello.core.domain.usecase.product.CountProductsUseCase
import com.samuelokello.core.domain.usecase.product.GetCategoriesUseCase
import com.samuelokello.core.domain.usecase.product.GetProductByIdUseCase
import com.samuelokello.core.domain.usecase.product.GetProductsPageUseCase
import com.samuelokello.core.domain.usecase.product.GetProductsUseCase
import com.samuelokello.core.domain.usecase.product.SearchProductsUseCase
import com.samuelokello.core.domain.usecase.product.SearchProductsWithFiltersUseCase
import com.samuelokello.data.repository.ProductRepositoryImpl
import com.samuelokello.data.repository.repository.AuthenticationRepositoryImpl
import com.samuelokello.data.repository.repository.CartRepositoryImpl
import com.samuelokello.data.repository.repository.LocalUserAccountRepositoryImpl
import com.samuelokello.data.repository.repository.OrderRepositoryImpl
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

        single<CartRepository> {
            CartRepositoryImpl(
                localSource = get(),
            )
        }

        single<OrderRepository> {
            OrderRepositoryImpl(
                localSource = get(),
            )
        }

        // Use Cases
        factory { GetProductsUseCase(get()) }
        factory { GetProductByIdUseCase(get()) }
        factory { GetCategoriesUseCase(get()) }
        factory { GetProductsPageUseCase(get()) }
        factory { CountProductsUseCase(get()) }
        factory { SearchProductsUseCase(get()) }
        factory { SearchProductsWithFiltersUseCase(get()) }

        factory { LoginUseCase(get()) }
        factory { RegisterUseCase(get()) }
        factory { ResetPasswordUseCase(get()) }
        factory { LogoutUseCase(get()) }
        factory { GetCurrentUserUseCase(get()) }

        factory { AddToCartUseCase(get()) }
        factory { ClearCartUseCase(get()) }

        factory { CreateOrderUseCase(get(), get(), get()) }
        factory { GetUserOrdersUseCase(get()) }
        factory { GetOrderByIdUseCase(get()) }
    }
