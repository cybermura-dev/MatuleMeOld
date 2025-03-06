package ru.takeshiko.matuleme.data.repository

import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.database.UserDeliveryAddress
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.repository.UserDeliveryAddressRepository

class UserDeliveryAddressRepositoryImpl(
    supabaseClientManager: SupabaseClientManager
) : UserDeliveryAddressRepository {

    private val postgrest = supabaseClientManager.postgrest

    override suspend fun getAddressesByUserId(userId: String): DataResult<List<UserDeliveryAddress>> {
        return try {
            val result = postgrest
                .from("user_delivery_addresses")
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserDeliveryAddress>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to get user delivery addresses!")
        }
    }

    override suspend fun addAddress(userDeliveryAddress: UserDeliveryAddress): DataResult<UserDeliveryAddress> {
        return try {
            val result = postgrest
                .from("user_delivery_addresses")
                .insert(userDeliveryAddress) { select() }
                .decodeSingle<UserDeliveryAddress>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to add user delivery addresses!")
        }
    }

    override suspend fun updateAddress(userId: String, addressId: String, address: String): DataResult<UserDeliveryAddress> {
        return try {
            val result = postgrest
                .from("user_delivery_addresses")
                .update({ set("address", address) }) { select(); filter { eq("id", addressId) } }
                .decodeSingle<UserDeliveryAddress>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update user delivery addresses!")
        }
    }

    override suspend fun removeAddress(userId: String, addressId: String): DataResult<String> {
        return try {
            postgrest
                .from("user_delivery_addresses")
                .delete {
                    filter {
                        eq("id", addressId)
                        eq("user_id", userId)
                    }
                }
            DataResult.Success(addressId)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to remove user delivery addresses!")
        }
    }
}