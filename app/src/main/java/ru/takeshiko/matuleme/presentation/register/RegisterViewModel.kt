package ru.takeshiko.matuleme.presentation.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.AuthRepositoryImpl
import ru.takeshiko.matuleme.domain.models.result.AuthResult

class RegisterViewModel(
    private val appPreferencesManager: AppPreferencesManager,
    supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _registrationResult = MutableLiveData<AuthResult<UserInfo?>>()
    val registrationResult: LiveData<AuthResult<UserInfo?>> = _registrationResult

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)

    fun registerUser(email: String, password: String, consent: Boolean) {
        viewModelScope.launch {
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _registrationResult.postValue(AuthResult.Error(appPreferencesManager.getString(R.string.invalid_email)))
                return@launch
            }

            if (password.isBlank() || password.length < 8) {
                _registrationResult.postValue(AuthResult.Error(appPreferencesManager.getString(R.string.password_min_length)))
                return@launch
            }

            if (!consent) {
                _registrationResult.postValue(AuthResult.Error(appPreferencesManager.getString(R.string.personal_data_consent_request)))
                return@launch
            }

            when (val result = authRepository.register(email, password)) {
                is AuthResult.Success -> _registrationResult.value = AuthResult.Success(result.data)
                is AuthResult.Error -> _registrationResult.value = AuthResult.Error(result.message)
            }
        }
    }
}
