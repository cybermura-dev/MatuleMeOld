package ru.takeshiko.matuleme.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivitySplashBinding
import ru.takeshiko.matuleme.domain.models.state.ActivityState
import ru.takeshiko.matuleme.presentation.login.LoginActivity
import ru.takeshiko.matuleme.presentation.main.MainActivity
import ru.takeshiko.matuleme.presentation.onboarding.OnboardingActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels {
        SplashViewModelFactory(
            AppPreferencesManager.getInstance(),
            SupabaseClientManager.getInstance()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            viewModel.activityNavigation.observe(this@SplashActivity) { activityState ->
                when (activityState) {
                    ActivityState.ONBOARDING -> {
                        startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
                        finish()
                    }

                    ActivityState.AUTH -> {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }

                    ActivityState.MAIN -> {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
                finish()
            }

            lifecycleScope.launch {
                delay(2000L)
                viewModel.navigateToNextActivity()
            }
        }
    }
}