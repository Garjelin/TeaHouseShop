package com.samuelokello.datasource.local.entity.user

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_user_accounts",
    indices = [Index(value = ["email"], unique = true)],
)
data class LocalUserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val salt: String,
)
