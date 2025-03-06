package ru.takeshiko.matuleme.presentation.checkout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)

        with (binding) {

        }
    }
}