package com.samuelokello.feat.profile.di

import com.samuelokello.feat.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule =
    module {
        viewModelOf(::ProfileViewModel)
    }