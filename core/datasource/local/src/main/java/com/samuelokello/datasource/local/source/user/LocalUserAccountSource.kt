package com.samuelokello.datasource.local.source.user

import com.samuelokello.datasource.local.db.user.LocalUserAccountDao
import com.samuelokello.datasource.local.entity.user.LocalUserAccountEntity
import com.samuelokello.datasource.local.source.preference.PreferenceHelper
import com.samuelokello.datasource.local.source.preference.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LocalUserAccountSource {
    suspend fun insertAccount(entity: LocalUserAccountEntity): Long

    suspend fun getByEmail(email: String): LocalUserAccountEntity?

    suspend fun getById(id: Long): LocalUserAccountEntity?

    fun observeCurrentUserId(): Flow<Long?>

    suspend fun setCurrentUserId(id: Long?)

    suspend fun clearSession()
}

class LocalUserAccountSourceImpl(
    private val dao: LocalUserAccountDao,
    private val sessionPreferences: PreferenceHelper,
) : LocalUserAccountSource {
    override suspend fun insertAccount(entity: LocalUserAccountEntity): Long = dao.insert(entity)

    override suspend fun getByEmail(email: String): LocalUserAccountEntity? = dao.getByEmail(email)

    override suspend fun getById(id: Long): LocalUserAccountEntity? = dao.getById(id)

    override fun observeCurrentUserId(): Flow<Long?> =
        sessionPreferences.get(PreferenceKeys.CURRENT_LOCAL_USER_ID).map { s ->
            s?.toLongOrNull()
        }

    override suspend fun setCurrentUserId(id: Long?) {
        if (id == null) {
            sessionPreferences.delete(PreferenceKeys.CURRENT_LOCAL_USER_ID)
        } else {
            sessionPreferences.save(PreferenceKeys.CURRENT_LOCAL_USER_ID, id.toString())
        }
    }

    override suspend fun clearSession() {
        sessionPreferences.delete(PreferenceKeys.CURRENT_LOCAL_USER_ID)
    }
}
