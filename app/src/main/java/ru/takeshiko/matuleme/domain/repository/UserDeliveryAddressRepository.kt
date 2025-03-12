package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.database.UserDeliveryAddress
import ru.takeshiko.matuleme.domain.models.result.DataResult

interface UserDeliveryAddressRepository {
    suspend fun getAddressesByUserId(userId: String): DataResult<List<UserDeliveryAddress>>
    suspend fun addAddress(userDeliveryAddress: UserDeliveryAddress): DataResult<UserDeliveryAddress>
    suspend fun updateAddressWithReset(userId: String, addressId: String, address: UserDeliveryAddress): DataResult<UserDeliveryAddress>
    suspend fun updateAddress(userId: String, addressId: String, address: UserDeliveryAddress): DataResult<UserDeliveryAddress>
    suspend fun removeAddress(userId: String, addressId: String): DataResult<String>
    suspend fun resetDefaultAddress(userId: String): DataResult<Unit>
}