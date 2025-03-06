package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.ProductCategory
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface ProductCategoryRepository {
    suspend fun getAll(): DataResult<List<ProductCategory>>
    suspend fun getCategoryById(categoryId: String): DataResult<ProductCategory>
}