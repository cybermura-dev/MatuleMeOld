package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.database.ProductCategory
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.ProductCategoryRepository

class ProductCategoryRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : ProductCategoryRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getAll(): DataResult<List<ProductCategory>> {
        return try {
            val result = postgrest
                .from("product_categories")
                .select()
                .decodeList<ProductCategory>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get product categories!")
        }
    }


    override suspend fun getCategoryById(categoryId: String): DataResult<ProductCategory> {
        return try {
            val result = postgrest
                .from("product_categories")
                .select { filter { eq("id", categoryId) } }
                .decodeSingle<ProductCategory>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get product by ID!")
        }
    }
}