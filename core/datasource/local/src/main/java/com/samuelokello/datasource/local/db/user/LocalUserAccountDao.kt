package com.samuelokello.datasource.local.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samuelokello.datasource.local.entity.user.LocalUserAccountEntity

@Dao
interface LocalUserAccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LocalUserAccountEntity): Long

    @Query("SELECT * FROM local_user_accounts WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): LocalUserAccountEntity?

    @Query("SELECT * FROM local_user_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LocalUserAccountEntity?

    @Query(
        """
        UPDATE local_user_accounts
        SET passwordHash = :passwordHash, salt = :salt
        WHERE email = :email
        """,
    )
    suspend fun updatePasswordByEmail(
        email: String,
        passwordHash: String,
        salt: String,
    ): Int
}
