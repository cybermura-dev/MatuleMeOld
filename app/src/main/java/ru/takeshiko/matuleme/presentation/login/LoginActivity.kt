package ru.takeshiko.matuleme.presentation.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.local.AppPreferencesManager
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityLoginBinding
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.presentation.main.MainActivity
import ru.takeshiko.matuleme.presentation.passwordreset.PasswordResetActivity
import ru.takeshiko.matuleme.presentation.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            AppPreferencesManager.getInstance(),
            SupabaseClientManager.getInstance()
        )
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@LoginActivity)

            tilPassword.setEndIconOnClickListener {
                togglePasswordVisibility(tilPassword, etPassword)
            }

            tvForgotPassword.setOnClickListener {
                handlePasswordReset()
            }

            tvRegisterPrompt.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
            }

            btnLogin.setOnClickListener {
                viewModel.loginUser(
                    etEmail.text.toString().trim(),
                    etPassword.text.toString().trim()
                )
            }

            viewModel.loginResult.observe(this@LoginActivity) { result ->
                when (result) {
                    is AuthResult.Success -> {
                        toast.show(
                            getString(R.string.successfully_login_title),
                            getString(R.string.successfully_login_message),
                            R.drawable.ic_checkmark,
                            onDismiss = {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
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

    private fun handlePasswordReset() {
        with (binding) {
            val email = etEmail.text.toString().trim()
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast.show(
                    getString(R.string.failed_title),
                    getString(R.string.write_email),
                    R.drawable.ic_cross
                )
                return
            }

            val intent = Intent(this@LoginActivity, PasswordResetActivity::class.java).apply {
                putExtra("email", email)
            }
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
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