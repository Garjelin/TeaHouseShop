package com.samuelokello.core.domain.util

sealed interface DataError : Error {
    enum class Network : DataError {
        NOT_FOUND,
        BAD_REQUEST,
        UNAUTHORIZED,
        FORBIDDEN,
        CONFLICT,
        SERVER_ERROR,
        NO_INTERNET,
        UNKNOWN,
        SERIALIZATION_ERROR,
        REQUEST_TIMEOUT,
        PAYLOAD_TOO_LARGE,
        TOO_MANY_REQUESTS,
    }

    enum class Local : DataError {
        DISK_FULL,
        CACHE_CORRUPTION,
        INVALID_DATA_FORMAT,
        PERMISSION_DENIED,
    }

    enum class Auth : DataError {
        EMAIL_ALREADY_EXISTS,
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        WEAK_PASSWORD,
        INVALID_EMAIL,
        UNKNOWN,
    }
}