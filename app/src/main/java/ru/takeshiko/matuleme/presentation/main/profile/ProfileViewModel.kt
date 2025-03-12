package ru.takeshiko.matuleme.presentation.main.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import ru.takeshiko.matuleme.data.repository.UserPaymentCardRepositoryImpl
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.domain.models.result.StorageResult
import java.io.ByteArrayOutputStream

class ProfileViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _userResult = MutableLiveData<DataResult<UserInfo>>()
    val userResult: LiveData<DataResult<UserInfo>> = _userResult

    private val _defaultAddress = MutableLiveData<String>()
    val defaultAddress: LiveData<String> get() = _defaultAddress

    private val _defaultPaymentCard = MutableLiveData<String>()
    val defaultPaymentCard: LiveData<String> get() = _defaultPaymentCard

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)
    private val storageRepository = StorageRepositoryImpl(supabaseClientManager)
    private val userDeliveryAddressRepository = UserDeliveryAddressRepositoryImpl(supabaseClientManager)
    private val userPaymentCardRepository = UserPaymentCardRepositoryImpl(supabaseClientManager)

    fun loadUserData() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                _userResult.value = DataResult.Success(user)
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun loadUserDefaultAddress() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                when (val result = userDeliveryAddressRepository.getAddressesByUserId(user.id)) {
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

    fun loadUserDefaultPaymentCard() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                when (val result = userPaymentCardRepository.getPaymentCardsByUserId(user.id)) {
                    is DataResult.Success -> {
                        val defaultPaymentCardEntry = result.data.find { it.isDefault }
                        _defaultPaymentCard.value = defaultPaymentCardEntry?.let { entry ->
                            val numberStr = entry.number.toString().padStart(16, '0')
                            "**** **** **** " + numberStr.takeLast(4)
                        } ?: ""
                    }
                    is DataResult.Error -> {
                        _defaultPaymentCard.value = ""
                        Log.d(javaClass.name, result.message)
                    }
                }
            }
        }
    }

    fun getAvatarFromUrlDefault(path: String): String {
        return "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/avatars/$path.png"
    }

    fun getAvatarFromUrl(path: String): String {
        return "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/avatars/$path"
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

    fun uploadAvatar(context: Context, fileUri: Uri) {
        viewModelScope.launch {
            val userId = supabaseClientManager.auth.currentUserOrNull()?.id ?: return@launch

            val fileExtension = "png"
            val path = "$userId.$fileExtension"
            val byteArray = convertToPng(context, fileUri)

            when (val result = storageRepository.uploadFile("avatars", path, byteArray)) {
                is StorageResult.Success -> {
                    val avatarUrl = getAvatarFromUrl(path) + "?timestamp=${System.currentTimeMillis()}"
                    updateAvatarMetadata(avatarUrl)
                }
                is StorageResult.Error -> Log.d(javaClass.name, result.message)
            }
        }
    }

    private fun updateAvatarMetadata(path: String) {
        viewModelScope.launch {
            when (val result = authRepository.updateUserData(avatarUrl = path)) {
                is AuthResult.Success -> loadUserData()
                is AuthResult.Error -> Log.d(javaClass.name, result.message)
            }
        }
    }

    private fun convertToPng(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ByteArray(0)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        inputStream.close()
        return outputStream.toByteArray()
    }
}