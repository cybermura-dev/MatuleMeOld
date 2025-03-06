package ru.takeshiko.matuleme.presentation.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.ActivityMenuBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.cart.CartActivity
import ru.takeshiko.matuleme.presentation.login.LoginActivity
import ru.takeshiko.matuleme.presentation.orders.OrdersActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private val viewModel: MenuViewModel by viewModels {
        MenuViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var toast: MaterialToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)


        with (binding) {
            setContentView(root)

            toast = MaterialToast(this@MenuActivity)

            viewModel.userResult.observe(this@MenuActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val userMetadata = result.data.userMetadata
                        if (userMetadata != null) {
                            val firstName = userMetadata["first_name"]?.toString()?.trim('"') ?: ""
                            val lastName = userMetadata["last_name"]?.toString()?.trim('"') ?: ""

                            tvData.text = if (firstName.isEmpty() && lastName.isEmpty()) {
                                getString(R.string.full_name_prompt, getString(R.string.no_data), "")
                            } else {
                                getString(R.string.full_name_prompt, firstName, lastName)
                            }

                            val avatarUrl = viewModel.getAvatarFromUrl(result.data.id)
                            Glide
                                .with(this@MenuActivity)
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .centerCrop()
                                .into(ivAvatar)
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            clLogout.setOnClickListener {
                viewModel.logoutUser()
                toast.show(
                    getString(R.string.successfully_logout_title),
                    getString(R.string.successfully_logout_message),
                    R.drawable.ic_checkmark,
                    onDismiss = {
                        val intent = Intent(this@MenuActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }

            clCart.setOnClickListener {
                startActivity(Intent(this@MenuActivity, CartActivity::class.java))
            }

            clOrders.setOnClickListener {
                startActivity(Intent(this@MenuActivity, OrdersActivity::class.java))
            }
        }
    }
}