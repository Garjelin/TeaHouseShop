package com.samuelokello.feat.cart.di

import com.samuelokello.feat.cart.CartViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cartModule =
    module {
        viewModelOf(::CartViewModel)
    }