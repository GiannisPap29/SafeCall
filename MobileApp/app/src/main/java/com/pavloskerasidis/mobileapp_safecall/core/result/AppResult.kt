package com.pavloskerasidis.mobileapp_safecall.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed class AppError(open val message: String, open val cause: Throwable? = null) {
    data class Network(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class Auth(override val message: String) : AppError(message)
    data class Permission(override val message: String) : AppError(message)
    data class Unknown(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> = also {
    if (it is AppResult.Success) block(it.value)
}

inline fun <T> AppResult<T>.onFailure(block: (AppError) -> Unit): AppResult<T> = also {
    if (it is AppResult.Failure) block(it.error)
}
