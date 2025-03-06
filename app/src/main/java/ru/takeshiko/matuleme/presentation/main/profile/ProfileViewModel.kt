package ru.takeshiko.matuleme.presentation.main.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.BuildConfig
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.AuthRepositoryImpl
import ru.takeshiko.matuleme.data.repository.StorageRepositoryImpl
import ru.takeshiko.matuleme.data.repository.UserDeliveryAddressRepositoryImpl
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.models.result.StorageResult

class ProfileViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _userResult = MutableLiveData<DataResult<UserInfo>>()
    val userResult: LiveData<DataResult<UserInfo>> = _userResult

    private val _defaultAddress = MutableLiveData<String>()
    val defaultAddress: LiveData<String> get() = _defaultAddress

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)
    private val storageRepository = StorageRepositoryImpl(supabaseClientManager)
    private val userDeliveryAddressRepository = UserDeliveryAddressRepositoryImpl(supabaseClientManager)

    fun loadUserData() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                _userResult.value = DataResult.Success(user)
            } ?: run {
                _userResult.value = DataResult.Error("User not authenticated!")
            }
        }
    }

    fun loadUserDefaultAddress() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                when(val result = userDeliveryAddressRepository.getAddressesByUserId(user.id)) {
                    is DataResult.Success -> {
                        val defaultAddressEntry = result.data.find { it.isDefault }
                        _defaultAddress.value = defaultAddressEntry?.address ?: ""
                    }
                    is DataResult.Error -> {
                        _defaultAddress.value = ""
                        Log.d(javaClass.name, result.message)
                    }
                }
            }
        }
    }

    fun getAvatarFromUrl(userId: String) : String {
        return BuildConfig.SUPABASE_URL +
                "/storage/v1/object/public/avatars//" +
                userId +
                ".png"
    }

    fun updateUserFirstName(firstName: String) {
        viewModelScope.launch {
            when (val result = authRepository.updateUserData(firstName = firstName)) {
                is AuthResult.Success -> loadUserData()
                is AuthResult.Error -> Log.d(javaClass.name, result.message)
            }
        }
    }

    fun updateUserLastName(lastName: String) {
        viewModelScope.launch {
            when (val result = authRepository.updateUserData(lastName = lastName)) {
                is AuthResult.Success -> loadUserData()
                is AuthResult.Error -> Log.d(javaClass.name, result.message)
            }
        }
    }

    fun updateUserPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            when (val result = authRepository.updateUserData(phoneNumber = phoneNumber)) {
                is AuthResult.Success -> loadUserData()
                is AuthResult.Error -> Log.d(javaClass.name, result.message)
            }
        }
    }

    fun uploadAvatar(file: ByteArray) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                val path = "$userId.png"

                when (val result = storageRepository.uploadFile("avatars", path, file)) {
                    is StorageResult.Success -> updateAvatarMetadata(path)
                    is StorageResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    private fun updateAvatarMetadata(path: String) {
        viewModelScope.launch {
            val avatarUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/avatars/$path"
            when (val result = authRepository.updateUserData(avatarUrl = avatarUrl)) {
                is AuthResult.Success -> loadUserData()
                is AuthResult.Error -> Log.d(javaClass.name, result.message)
            }
        }
    }
}