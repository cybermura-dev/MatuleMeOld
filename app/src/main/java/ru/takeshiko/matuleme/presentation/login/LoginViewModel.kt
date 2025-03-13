package ru.takeshiko.matuleme.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.AuthRepositoryImpl
import ru.takeshiko.matuleme.domain.models.result.AuthResult

class LoginViewModel(
    private val appPreferencesManager: AppPreferencesManager,
    supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<AuthResult<Unit>>()
    val loginResult: LiveData<AuthResult<Unit>> = _loginResult

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _loginResult.postValue(AuthResult.Error(appPreferencesManager.getString(R.string.invalid_email)))
                return@launch
            }

            if (password.length < 8) {
                _loginResult.postValue(AuthResult.Error(appPreferencesManager.getString(R.string.password_min_length)))
                return@launch
            }

            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> _loginResult.value = AuthResult.Success(result.data)
                is AuthResult.Error -> _loginResult.value = AuthResult.Error(result.message)
            }
        }
    }
}
