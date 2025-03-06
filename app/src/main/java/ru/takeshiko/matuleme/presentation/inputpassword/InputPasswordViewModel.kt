package ru.takeshiko.matuleme.presentation.inputpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.AuthRepositoryImpl
import ru.takeshiko.matuleme.domain.models.result.AuthResult

class InputPasswordViewModel(
    supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _changePasswordResult = MutableLiveData<AuthResult<Any>>()
    val changePasswordResult: LiveData<AuthResult<Any>> = _changePasswordResult

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)

    fun changePasswordUser(email: String, newPassword: String, newPasswordVerify: String) {
        viewModelScope.launch {
            TODO()
        }
    }
}