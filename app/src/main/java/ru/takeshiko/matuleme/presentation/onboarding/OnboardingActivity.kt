package ru.takeshiko.matuleme.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.databinding.ActivityOnboardingBinding
import ru.takeshiko.matuleme.presentation.login.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentPageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            val appPreferencesManager = AppPreferencesManager.getInstance()
            val adapter = OnboardingPagerAdapter(this@OnboardingActivity)

            viewPager.apply {
                this.adapter = adapter
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        currentPageIndex = position
                        btnNext.text = if (isLastPage()) getString(R.string.start) else getString(R.string.next)
                    }
                })
            }

            btnNext.setOnClickListener {
                if (!isLastPage()) {
                    viewPager.currentItem += 1
                } else {
                    appPreferencesManager.isFirstLaunch = false
                    startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun isLastPage(): Boolean = currentPageIndex == 2
}