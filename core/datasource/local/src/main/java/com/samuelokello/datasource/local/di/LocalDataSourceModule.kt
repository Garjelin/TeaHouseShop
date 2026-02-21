package com.samuelokello.datasource.local.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.samuelokello.datasource.local.db.ShopSpotDB
import com.samuelokello.datasource.local.source.auth.AuthenticationLocalSource
import com.samuelokello.datasource.local.source.auth.AuthenticationLocalSourceImpl
import com.samuelokello.datasource.local.source.preference.PreferenceHelper
import com.samuelokello.datasource.local.source.preference.PreferencesHelperImpl
import com.samuelokello.datasource.local.source.product.ProductLocalSource
import com.samuelokello.datasource.local.source.product.ProductLocalSourceImpl
import com.samuelokello.datasource.local.util.MockDataInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.localPreferences by preferencesDataStore("local_preferences")
private val Context.remotePreferences by preferencesDataStore("remote_preferences")

val LocalQualifier = named("local")
val RemoteQualifier = named("remote")

val localDataSourceModule =
    module {
        single<ShopSpotDB> {
            Room
                .databaseBuilder(
                    context = androidContext(),
                    klass = ShopSpotDB::class.java,
                    name = "shopspot.db",
                ).build()
        }

        single<PreferenceHelper>(LocalQualifier) {
            PreferencesHelperImpl(get<Context>().localPreferences)
        }

        single<PreferenceHelper>(RemoteQualifier) {
            PreferencesHelperImpl(get<Context>().remotePreferences)
        }

        single<AuthenticationLocalSource> {
            AuthenticationLocalSourceImpl(get(RemoteQualifier))
        }

        // Product DAO
        single { get<ShopSpotDB>().productDao() }

        // Product Local Source
        single<ProductLocalSource> {
            ProductLocalSourceImpl(get())
        }

        // Mock Data Initializer
        single {
            MockDataInitializer(get(), androidContext())
        }
    }