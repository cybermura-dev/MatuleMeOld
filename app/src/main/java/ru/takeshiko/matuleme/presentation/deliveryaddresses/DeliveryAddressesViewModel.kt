package ru.takeshiko.matuleme.presentation.deliveryaddresses

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

class DeliveryAddressesViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _addressesResult = MutableLiveData<DataResult<List<UserDeliveryAddress>>>()
    val addressesResult: LiveData<DataResult<List<UserDeliveryAddress>>> = _addressesResult

    private val userDeliveryAddressRepository = UserDeliveryAddressRepositoryImpl(supabaseClientManager)

    fun loadUserAddresses() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userDeliveryAddressRepository.getAddressesByUserId(userId)) {
                    is DataResult.Success -> {
                        val sortedAddresses = result.data.sortedByDescending { it.isDefault }
                        _addressesResult.value = DataResult.Success(sortedAddresses)
                    }
                    is DataResult.Error -> _addressesResult.value = DataResult.Error(result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun removeAddress(address: UserDeliveryAddress) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userDeliveryAddressRepository.removeAddress(userId, address.id!!)) {
                    is DataResult.Success -> {
                        val updatedAddresses = _addressesResult.value?.let { dataResult ->
                            if (dataResult is DataResult.Success) {
                                dataResult.data.filter { it.id != address.id }
                            } else {
                                emptyList()
                            }
                        } ?: emptyList()

                        if (address.isDefault && updatedAddresses.isNotEmpty()) {
                            setDefaultAddress(updatedAddresses.first())
                        } else {
                            _addressesResult.value = DataResult.Success(updatedAddresses.sortedByDescending { it.isDefault })
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun setDefaultAddress(address: UserDeliveryAddress) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (userDeliveryAddressRepository.resetDefaultAddress(userId)) {
                    is DataResult.Success -> {
                        val updatedAddress = address.copy(isDefault = true)
                        when (val updateResult = userDeliveryAddressRepository.updateAddressWithReset(userId, updatedAddress.id!!, updatedAddress)) {
                            is DataResult.Success -> loadUserAddresses()
                            is DataResult.Error -> Log.d(javaClass.name, updateResult.message)
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, "Failed to reset default address!")
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}