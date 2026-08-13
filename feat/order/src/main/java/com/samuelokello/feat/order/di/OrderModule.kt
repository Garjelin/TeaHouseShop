package com.samuelokello.feat.order.di

import com.samuelokello.feat.order.CheckoutViewModel
import com.samuelokello.feat.order.OrderDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val orderModule =
    module {
        viewModelOf(::CheckoutViewModel)
        viewModelOf(::OrderDetailViewModel)
    }
