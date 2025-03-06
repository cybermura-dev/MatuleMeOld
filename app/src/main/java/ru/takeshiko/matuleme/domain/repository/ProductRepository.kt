package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface ProductRepository {
    suspend fun getAll(): DataResult<List<Product>>
    suspend fun getByCategory(categoryId: String): DataResult<List<Product>>
    suspend fun getProductsByIds(ids: List<String>): DataResult<List<Product>>
    suspend fun getProductById(productId: String): DataResult<Product>
    suspend fun getProductsByQuery(query: String): DataResult<List<Product>>
}