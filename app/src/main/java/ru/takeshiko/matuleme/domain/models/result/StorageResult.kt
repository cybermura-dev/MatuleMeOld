package ru.takeshiko.matuleme.domain.models.result

sealed class StorageResult<out T> {
    data class Success<out T>(val data: T) : StorageResult<T>()
    data class Error(val message: String) : StorageResult<Nothing>()
}