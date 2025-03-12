package ru.takeshiko.matuleme.presentation.passwordreset

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
import ru.takeshiko.matuleme.databinding.ActivityPasswordResetBinding
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.presentation.inputpassword.InputPasswordActivity

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordResetBinding
    private val viewModel: PasswordResetViewModel by viewModels {
        PasswordResetViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)

        toast = MaterialToast(this)

        with (binding) {
            setContentView(root)

            val email = intent.getStringExtra("email")

            etSecretCode.addTextChangedListener(object : TextWatcher {
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

            tvSendAgain.setOnClickListener { sendSecret(email!!) }

            viewModel.timerLiveData.observe(this@PasswordResetActivity) { time ->
                tvTime.text = time
                tvSendAgain.isVisible = time.isEmpty()
            }

            viewModel.verifyResult.observe(this@PasswordResetActivity) { result ->
                when (result) {
                    is AuthResult.Success -> {
                        toast.show(
                            getString(R.string.successfully_reset_title),
                            getString(R.string.successfully_reset_message),
                            R.drawable.ic_policy_check,
                            onDismiss = {
                                val intent = Intent(this@PasswordResetActivity, InputPasswordActivity::class.java).apply {
                                    putExtra("email", email)
                                }
                                startActivity(intent)
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
                                tilSecretCode.boxStrokeColor = ContextCompat.getColor(this@PasswordResetActivity, R.color.error_color)
                            }
                        )
                    }
                }
            }

            sendSecret(email!!)
        }
    }

    private fun sendSecret(email: String) {
        lifecycleScope.launch {
            viewModel.resendSecret(email).also {
                viewModel.startTimer()
                toast.show(
                    getString(R.string.check_email_title),
                    getString(R.string.reset_password_sent),
                    R.drawable.ic_sended_email
                )
            }
        }
    }
}