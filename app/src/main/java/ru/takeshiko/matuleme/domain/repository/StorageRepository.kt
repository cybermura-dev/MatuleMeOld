package ru.takeshiko.matuleme.domain.repository

import io.github.jan.supabase.storage.FileObject
import io.github.jan.supabase.storage.FileUploadResponse
import ru.takeshiko.matuleme.domain.models.result.StorageResult

interface StorageRepository {
    suspend fun getAll(bucket: String): StorageResult<List<FileObject>>
    suspend fun uploadFile(bucket: String, path: String, file: ByteArray): StorageResult<FileUploadResponse>
}