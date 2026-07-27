package com.smartpoultry.app.domain.model

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Failure(val exception: Throwable) : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> exception
    }

    inline fun onSuccess(action: (value: @UnsafeVariance T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (exception: Throwable) -> Unit): AppResult<T> {
        if (this is Failure) action(exception)
        return this
    }

    inline fun <R> map(transform: (value: @UnsafeVariance T) -> R): AppResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Failure -> Failure(exception)
        }
    }

    inline fun getOrElse(onFailure: (exception: Throwable) -> @UnsafeVariance T): @UnsafeVariance T {
        return when (this) {
            is Success -> data
            is Failure -> onFailure(exception)
        }
    }
}
