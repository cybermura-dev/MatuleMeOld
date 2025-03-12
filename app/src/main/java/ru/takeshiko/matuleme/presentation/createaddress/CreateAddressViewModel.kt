package ru.takeshiko.matuleme.presentation.createaddress

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserDeliveryAddressRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserDeliveryAddress
import ru.takeshiko.matuleme.domain.models.result.DataResult

class CreateAddressViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val userDeliveryAddressRepository = UserDeliveryAddressRepositoryImpl(supabaseClientManager)

    fun saveAddress(newAddress: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val addressesResult = userDeliveryAddressRepository.getAddressesByUserId(userId)) {
                    is DataResult.Success -> {
                        val existingAddresses = addressesResult.data
                        val isFirstAddress = existingAddresses.isEmpty()

                        val addressToSave = UserDeliveryAddress(
                            userId = userId,
                            address = newAddress,
                            isDefault = isFirstAddress
                        )

                        when (val saveResult = userDeliveryAddressRepository.addAddress(addressToSave)) {
                            is DataResult.Success -> {
                                if (!isFirstAddress) {
                                    existingAddresses.forEach {
                                        userDeliveryAddressRepository.updateAddress(userId, it.id!!, it)
                                    }
                                }
                            }
                            is DataResult.Error -> Log.d(javaClass.name, saveResult.message)
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, addressesResult.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}