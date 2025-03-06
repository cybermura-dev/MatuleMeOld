package ru.takeshiko.matuleme.presentation.main.notifications

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.R
import ru.takeshiko.matuleme.data.adapters.NotificationCardShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.utils.MaterialToast
import ru.takeshiko.matuleme.databinding.FragmentNotificationsBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var viewModel: NotificationsViewModel
    private lateinit var shimmerAdapter: NotificationCardShimmerAdapter
    private lateinit var toast: MaterialToast

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationsBinding.bind(view)

        toast = MaterialToast(requireContext())

        val factory = NotificationsViewModelFactory(SupabaseClientManager.getInstance())
        viewModel = ViewModelProvider(this, factory)[NotificationsViewModel::class.java]

        with (binding) {
            shimmerAdapter = NotificationCardShimmerAdapter(6)

            rvNotifications.apply {
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        setDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.vertical_divider
                            )!!
                        )
                    }
                )
                adapter = shimmerAdapter
            }

            viewModel.notificationsResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is DataResult.Success -> {
                        val notifications = result.data
                        rvNotifications.adapter = NotificationCardAdapter(
                            requireContext(),
                            notifications
                        ) { notification ->
                            if (!notification.isRead)
                                viewModel.markAsRead(notification.id!!)
                        }
                    }

                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllNotifications()
    }
}