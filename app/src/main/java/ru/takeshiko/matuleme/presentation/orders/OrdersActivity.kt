package ru.takeshiko.matuleme.presentation.orders

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.takeshiko.matuleme.databinding.ActivityOrdersBinding

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)
        }
    }
}