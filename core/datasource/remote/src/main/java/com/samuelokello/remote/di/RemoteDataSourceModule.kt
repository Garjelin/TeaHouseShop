package com.samuelokello.remote.di

import com.samuelokello.remote.sources.auth.AuthenticationRemoteSource
import com.samuelokello.remote.sources.auth.AuthenticationRemoteSourceImpl
import com.samuelokello.remote.sources.product.ProductRemoteSource
import com.samuelokello.remote.sources.product.ProductRemoteSourceImpl
import com.samuelokello.remote.util.HttpClientFactory
import io.ktor.client.HttpClient
import org.koin.dsl.module

val remoteDataSourceModule =
    module {
        single<HttpClient> {
            HttpClientFactory.create()
        }
        single<AuthenticationRemoteSource> { AuthenticationRemoteSourceImpl(httpClient = get()) }
        single<ProductRemoteSource> { ProductRemoteSourceImpl() }
    }