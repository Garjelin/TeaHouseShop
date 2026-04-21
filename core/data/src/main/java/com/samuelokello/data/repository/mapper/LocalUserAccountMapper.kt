package com.samuelokello.data.repository.mapper

import com.samuelokello.core.domain.model.UserProfile
import com.samuelokello.datasource.local.entity.user.LocalUserAccountEntity

fun LocalUserAccountEntity.toUserProfile(): UserProfile =
    UserProfile(
        id = id,
        email = email,
        displayName = displayName,
    )
