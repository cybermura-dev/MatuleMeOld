package ru.takeshiko.matuleme.domain.models.result

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val message: String) : DataResult<Nothing>()
}