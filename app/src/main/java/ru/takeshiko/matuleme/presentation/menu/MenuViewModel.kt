package ru.takeshiko.matuleme.presentation.menu

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
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.domain.models.result.DataResult

class MenuViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _userResult = MutableLiveData<DataResult<UserInfo>>()
    val userResult: LiveData<DataResult<UserInfo>> = _userResult

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)

    fun loadUserData() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.let { user ->
                _userResult.value = DataResult.Success(user)
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            when (val result = authRepository.logout()) {
                is AuthResult.Success -> AuthResult.Success(result.data)
                is AuthResult.Error -> AuthResult.Error(result.message)
            }
        }
    }

    fun getAvatarFromUrl(userId: String) : String {
        return "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/avatars/$userId.png"
    }
}