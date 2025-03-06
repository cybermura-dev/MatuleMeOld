package ru.takeshiko.matuleme.presentation.otpverify

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.AuthRepositoryImpl
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import java.util.Locale

class OTPVerifyViewModel(
    supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _verifyResult = MutableLiveData<AuthResult<Any>>()
    val verifyResult: LiveData<AuthResult<Any>> = _verifyResult

    private val _timerLiveData = MutableLiveData<String>()
    val timerLiveData: LiveData<String> = _timerLiveData

    private val authRepository = AuthRepositoryImpl(supabaseClientManager)
    private var countDownTimer: CountDownTimer? = null

    fun verifyUser(email: String, otp: String) {
        viewModelScope.launch {
            try {
                when (val result = authRepository.verifyEmail(email, otp)) {
                    is AuthResult.Success -> _verifyResult.value = AuthResult.Success(result.data)
                    is AuthResult.Error -> _verifyResult.value = AuthResult.Error(result.message)
                }
            } catch (e: Exception) {
                _verifyResult.value = AuthResult.Error(e.message ?: "Email verification failed!")
            }
        }
    }

    fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                _timerLiveData.value = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    seconds / 60,
                    seconds % 60
                )
            }

            override fun onFinish() {
                _timerLiveData.value = ""
            }
        }.start()
    }

    suspend fun resendOtp(email: String): AuthResult<Any> {
        return try {
            when (val result = authRepository.sendVerificationEmail(email)) {
                is AuthResult.Success -> AuthResult.Success(result)
                is AuthResult.Error -> AuthResult.Error(result.message)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to resend OTP code!")
        }
    }


    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}