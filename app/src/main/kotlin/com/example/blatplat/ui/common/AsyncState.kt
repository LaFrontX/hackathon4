package com.example.blatplat.ui.common

/**
 * Унифицированное состояние асинхронных экранов: один компонент UI решает, что показать.
 */
sealed interface AsyncState<out T> {
    data object Loading : AsyncState<Nothing>
    data class Success<T>(val value: T) : AsyncState<T>
    data class Error(val message: String, val retry: () -> Unit) : AsyncState<Nothing>
}
