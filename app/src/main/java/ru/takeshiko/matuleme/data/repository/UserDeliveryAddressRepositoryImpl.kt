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

    override suspend fun updateAddress(userId: String, addressId: String, address: UserDeliveryAddress): DataResult<UserDeliveryAddress> {
        return try {
            val result = postgrest
                .from("user_delivery_addresses")
                .update(address) {
                    filter { eq("id", addressId) }
                    select()
                }
                .decodeSingle<UserDeliveryAddress>()
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to update user delivery addresses!")
        }
    }

    override suspend fun updateAddressWithReset(userId: String, addressId: String, address: UserDeliveryAddress): DataResult<UserDeliveryAddress> {
        return try {
            resetDefaultAddress(userId)
            val result = postgrest
                .from("user_delivery_addresses")
                .update(address) {
                    filter { eq("id", addressId) }
                    select()
                }
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

    override suspend fun resetDefaultAddress(userId: String): DataResult<Unit> {
        return try {
            postgrest
                .from("user_delivery_addresses")
                .update({ set("is_default", false) })
                { filter { eq("user_id", userId) } }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to reset default address!")
        }
    }
}