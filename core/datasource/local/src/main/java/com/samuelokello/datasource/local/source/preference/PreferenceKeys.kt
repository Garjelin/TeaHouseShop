package com.samuelokello.datasource.local.source.preference

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    /** ID записи в [com.samuelokello.datasource.local.entity.user.LocalUserAccountEntity] */
    val CURRENT_LOCAL_USER_ID = stringPreferencesKey("current_local_user_id")
}