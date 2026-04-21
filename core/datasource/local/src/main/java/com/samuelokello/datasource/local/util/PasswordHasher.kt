package com.samuelokello.datasource.local.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
    private const val SALT_BYTES = 16

    fun generateSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(
        password: String,
        salt: String,
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$password".toByteArray(Charsets.UTF_8)
        val bytes = digest.digest(input)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun verify(
        password: String,
        salt: String,
        expectedHash: String,
    ): Boolean = hash(password, salt) == expectedHash
}
