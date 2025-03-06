package ru.takeshiko.matuleme.data.repository

import android.util.Log
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.Product
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.ProductRepository

class ProductRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : ProductRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getAll(): DataResult<List<Product>> {
        return try {
            val result = postgrest.from("products")
                .select()
                .decodeList<Product>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get products!")
        }
    }

    override suspend fun getByCategory(categoryId: String): DataResult<List<Product>> {
        return try {
            val result = postgrest.from("products")
                .select { filter { eq("category_id", categoryId) } }
                .decodeList<Product>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get products by category!")
        }
    }

    override suspend fun getProductsByIds(ids: List<String>): DataResult<List<Product>> {
        return try {
            val result = postgrest
                .from("products")
                .select { filter { isIn("id", ids) } }
                .decodeList<Product>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get products by IDs!")
        }
    }

    override suspend fun getProductById(productId: String): DataResult<Product> {
        return try {
            val result = postgrest
                .from("products")
                .select { filter { eq("id", productId) } }
                .decodeSingle<Product>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get product by ID!")
        }
    }

    override suspend fun getProductsByQuery(query: String): DataResult<List<Product>> {
        return try {
            val searchQuery = "%${query.trim()}%"

            val result = postgrest
                .from("products")
                .select {
                    filter {
                        or {
                            Product::title ilike searchQuery
                            Product::description ilike searchQuery
                        }
                    }
                }
                .decodeList<Product>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get products by query!")
        }
    }
}