package ru.takeshiko.matuleme.presentation.editaddress

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserDeliveryAddressRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserDeliveryAddress
import ru.takeshiko.matuleme.domain.models.result.DataResult

class EditAddressViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val userDeliveryAddressRepository = UserDeliveryAddressRepositoryImpl(supabaseClientManager)

    private val _address = MutableLiveData<UserDeliveryAddress?>()
    val address: LiveData<UserDeliveryAddress?> = _address

    fun loadAddress(addressId: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userDeliveryAddressRepository.getAddressesByUserId(userId)) {
                    is DataResult.Success -> {
                        val foundAddress = result.data.find { it.id == addressId }
                        _address.value = foundAddress
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun updateAddress(addressId: String, newAddress: String, isDefault: Boolean) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                val updatedAddress = _address.value?.copy(address = newAddress, isDefault = isDefault)

                if (updatedAddress != null) {
                    when (val result = userDeliveryAddressRepository.updateAddress(userId, addressId, updatedAddress)) {
                        is DataResult.Success -> Log.d(javaClass.name, "Address updated successfully!")
                        is DataResult.Error -> Log.d(javaClass.name, result.message)
                    }
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}