package com.samuelokello.datasource.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `local_user_accounts` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `email` TEXT NOT NULL,
                    `displayName` TEXT NOT NULL,
                    `passwordHash` TEXT NOT NULL,
                    `salt` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_local_user_accounts_email` ON `local_user_accounts` (`email`)",
            )
        }
    }
