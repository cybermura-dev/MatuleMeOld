package ru.takeshiko.matuleme.presentation.register

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.auth.user.UserInfo
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityRegisterBinding
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.presentation.login.LoginActivity
import ru.takeshiko.matuleme.presentation.otpverify.OTPVerifyActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(
            AppPreferencesManager.getInstance(),
            SupabaseClientManager.getInstance()
        )
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@RegisterActivity)

            tilPassword.setEndIconOnClickListener {
                togglePasswordVisibility(tilPassword, etPassword)
            }

            tvLoginPrompt.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
            }

            btnRegister.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val consent = cbConsent.isChecked

                viewModel.registerUser(email, password, consent)
            }

            viewModel.registrationResult.observe(this@RegisterActivity) { result ->
                when (result) {
                    is AuthResult.Success -> {
                        val data = result.data!!
                        if (data.emailConfirmedAt != null) {
                            toast.show(
                                getString(R.string.failed_title),
                                getString(R.string.email_already_registered),
                                R.drawable.ic_cross
                            )
                            return@observe
                        }

                        toast.show(
                            getString(R.string.successfully_register_title),
                            getString(R.string.successfully_register_message),
                            R.drawable.ic_checkmark,
                            onDismiss = {
                                val intent = Intent(this@RegisterActivity, OTPVerifyActivity::class.java).apply {
                                    putExtra("email", etEmail.text.toString().trim())
                                }
                                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                startActivity(intent)
                            }
                        )
                    }
                    is AuthResult.Error -> {
                        toast.show(
                            getString(R.string.failed_title),
                            result.message,
                            R.drawable.ic_cross
                        )
                    }
                }
            }
        }
    }

    private fun togglePasswordVisibility(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText) {
        val isPasswordVisible = textInputEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        if (isPasswordVisible) {
            textInputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_eye_close)
        } else {
            textInputEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            textInputLayout.endIconDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_eye_open)
        }

        textInputEditText.setSelection(textInputEditText.text?.length ?: 0)
    }
}
