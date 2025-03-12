package ru.takeshiko.matuleme.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivityMainBinding
import ru.takeshiko.matuleme.databinding.BottomNavigationBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.cart.CartActivity
import ru.takeshiko.matuleme.presentation.main.favorites.FavoritesFragment
import ru.takeshiko.matuleme.presentation.main.home.HomeFragment
import ru.takeshiko.matuleme.presentation.main.notifications.NotificationsFragment
import ru.takeshiko.matuleme.presentation.main.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var bottomNavigation: BottomNavigationBinding

    private val fragmentMap: Map<ImageView, Fragment> by lazy {
        mapOf(
            bottomNavigation.btnHome to HomeFragment(),
            bottomNavigation.btnFavorites to FavoritesFragment(),
            bottomNavigation.btnNotifications to NotificationsFragment(),
            bottomNavigation.btnProfile to ProfileFragment()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            this@MainActivity.bottomNavigation = bottomNavigation

            fragmentMap.forEach { (button, fragment) ->
                button.setOnClickListener { onNavigationButtonClicked(button, fragment) }
            }

            bottomNavigation.btnCart.setOnClickListener {
                startActivity(Intent(this@MainActivity, CartActivity::class.java))
            }

            viewModel.cartItemsResult.observe(this@MainActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        if (result.data.isNotEmpty()) {
                            bottomNavigation.cartBadge.visibility = View.VISIBLE
                            bottomNavigation.cartBadge.text = result.data.size.toString()
                        } else {
                            bottomNavigation.cartBadge.visibility = View.GONE
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            viewModel.unreadNotificationsResult.observe(this@MainActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        if (result.data.isNotEmpty()) {
                            bottomNavigation.notificationBadge.visibility = View.VISIBLE
                            bottomNavigation.notificationBadge.text = result.data.size.toString()
                        } else {
                            bottomNavigation.notificationBadge.visibility = View.GONE
                        }
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }

            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
                updateSelectedButton(bottomNavigation.btnHome)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startNotificationsPolling()
    }

    private fun onNavigationButtonClicked(button: ImageView, fragment: Fragment) {
        replaceFragment(fragment)
        updateSelectedButton(button)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateSelectedButton(selectedButton: ImageView) {
        val buttons = listOf(
            bottomNavigation.btnHome,
            bottomNavigation.btnFavorites,
            bottomNavigation.btnNotifications,
            bottomNavigation.btnProfile
        )

        buttons.forEach { button ->
            button.setColorFilter(
                ContextCompat.getColor(
                    this,
                    if (button == selectedButton) R.color.accent_color
                    else R.color.sub_text_dark_color
                ),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }
}