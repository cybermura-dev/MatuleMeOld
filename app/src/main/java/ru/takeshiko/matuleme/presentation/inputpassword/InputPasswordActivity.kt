package ru.takeshiko.matuleme.presentation.inputpassword

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityInputPasswordBinding
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.presentation.login.LoginActivity

class InputPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputPasswordBinding
    private val viewModel: InputPasswordViewModel by viewModels {
        InputPasswordViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPasswordBinding.inflate(layoutInflater)


        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@InputPasswordActivity)

            val email = intent.getStringExtra("email")

            tilNewPassword.setEndIconOnClickListener {
                togglePasswordVisibility(tilNewPassword, etNewPassword)
            }

            tilNewPasswordVerify.setEndIconOnClickListener {
                togglePasswordVisibility(tilNewPasswordVerify, etNewPasswordVerify)
            }

            btnChangePassword.setOnClickListener {
                val newPassword = etNewPassword.text.toString().trim()
                val newPasswordVerify = etNewPasswordVerify.text.toString().trim()
                viewModel.changePasswordUser(email ?: "", newPassword, newPasswordVerify)
            }

            viewModel.changePasswordResult.observe(this@InputPasswordActivity) { result ->
                when (result) {
                    is AuthResult.Success -> {
                        startActivity(Intent(this@InputPasswordActivity, LoginActivity::class.java))
                        finish()
                    }

                    is AuthResult.Error -> {
                        toast.show(
                            getString(R.string.failed_title),
                            result.message,
                            R.drawable.ic_cross
                        )
                        Log.d(javaClass.name, result.message)
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