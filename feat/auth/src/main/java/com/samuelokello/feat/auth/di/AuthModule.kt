package com.samuelokello.feat.auth.di

import com.samuelokello.feat.auth.presentation.login.LoginViewModel
import com.samuelokello.feat.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.samuelokello.feat.auth.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule =
    module {
        viewModelOf(::LoginViewModel)
        viewModelOf(::RegisterViewModel)
        viewModelOf(::ForgotPasswordViewModel)
    }
