package ru.takeshiko.matuleme.data.repository

import io.github.jan.supabase.storage.FileObject
import io.github.jan.supabase.storage.FileUploadResponse
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.result.StorageResult
import ru.takeshiko.matuleme.domain.repository.StorageRepository

class StorageRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : StorageRepository {

    private val storage = supabaseClientManager.storage

    override suspend fun getAll(bucket: String): StorageResult<List<FileObject>> {
        return try {
            val result = storage
                .from(bucket)
                .list()

            val filteredResult = result.filter { it.name != ".emptyFolderPlaceholder" }

            StorageResult.Success(filteredResult)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Failed to get files!")
        }
    }


    override suspend fun uploadFile(
        bucket: String,
        path: String,
        file: ByteArray
    ): StorageResult<FileUploadResponse> {
        return try {
            val result = storage
                .from(bucket)
                .upload(path, file) { upsert = true }
            StorageResult.Success(result)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Failed to upload file!")
        }
    }
}