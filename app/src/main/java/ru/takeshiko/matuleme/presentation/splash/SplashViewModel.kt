package ru.takeshiko.matuleme.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.AuthRepositoryImpl
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.domain.models.state.ActivityState

class SplashViewModel(
    private val appPreferencesManager: AppPreferencesManager,
    supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)

    private val _activityNavigation = MutableLiveData<ActivityState>()
    val activityNavigation: LiveData<ActivityState> get() = _activityNavigation

    fun navigateToNextActivity() {
        viewModelScope.launch {
            _activityNavigation.postValue(
                when {
                    appPreferencesManager.isFirstLaunch -> ActivityState.ONBOARDING
                    else -> when (val authResult = authRepository.isUserAuthenticated()) {
                        is AuthResult.Success -> if (authResult.data) ActivityState.MAIN else ActivityState.AUTH
                        else -> ActivityState.AUTH
                    }
                }
            )
        }
    }
}