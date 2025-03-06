package ru.takeshiko.matuleme.presentation.otpverify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityOtpVerifyBinding
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.presentation.login.LoginActivity

class OTPVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerifyBinding
    private val viewModel: OTPVerifyViewModel by viewModels {
        OTPVerifyViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)

        toast = MaterialToast(this)

        with (binding) {
            setContentView(root)

            val email = intent.getStringExtra("email")

            etOtpCode.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 6) {
                        val otpCode = s.toString()
                        viewModel.verifyUser(email!!, otpCode)
                    }
                }
            })

            tvSendAgain.setOnClickListener { sendOtp(email!!) }

            viewModel.timerLiveData.observe(this@OTPVerifyActivity) { time ->
                tvTime.text = time
                tvSendAgain.isVisible = time.isEmpty()
            }

            viewModel.verifyResult.observe(this@OTPVerifyActivity) { result ->
                when (result) {
                    is AuthResult.Success -> {
                        toast.show(
                            getString(R.string.successfully_verify_title),
                            getString(R.string.successfully_verify_message),
                            R.drawable.ic_policy_check,
                            onDismiss = {
                                startActivity(Intent(this@OTPVerifyActivity, LoginActivity::class.java))
                                finish()
                            }
                        )
                    }

                    is AuthResult.Error -> {
                        toast.show(
                            getString(R.string.failed_title),
                            result.message,
                            R.drawable.ic_cross,
                            onDismiss = {
                                tilOtpCode.boxStrokeColor = ContextCompat.getColor(this@OTPVerifyActivity, R.color.error_color)
                            }
                        )
                    }
                }
            }

            sendOtp(email!!)
        }
    }

    private fun sendOtp(email: String) {
        lifecycleScope.launch {
            viewModel.resendOtp(email).also {
                viewModel.startTimer()
                toast.show(
                    getString(R.string.check_email_title),
                    getString(R.string.verify_email_sent),
                    R.drawable.ic_sended_email
                )
            }
        }
    }
}