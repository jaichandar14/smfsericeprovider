package com.smf.events.ui.notification.oldnotification

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentOldNotificationBinding
import com.smf.events.ui.notification.adapter.NotificationAdapter
import com.smf.events.ui.notification.model.NotificationDetails
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class OldNotificationFragment :
    BaseFragment<FragmentOldNotificationBinding, OldNotificationViewModel>(),
    NotificationAdapter.OnNotificationClickListener {

    var TAG = "OldNotificationFragment"
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: NotificationAdapter
    private val notificationList = ArrayList<NotificationDetails>()

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): OldNotificationViewModel =
        ViewModelProvider(this, factory).get(OldNotificationViewModel::class.java)

    override fun getBindingVariable(): Int = BR.oldnotificationViewModel

    override fun getContentView(): Int = R.layout.fragment_old_notification

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initializing RecyclerView
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        // Initialize RecyclerView Active
        recyclerView = mDataBinding!!.oldNotificationRecycler
        adapter = NotificationAdapter()
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.setOnClickListener(this)
        fakeData()
    }

    private fun fakeData() {
        notificationList.clear()
        val obj = NotificationDetails(
            "10/May/2022 6:10 am",
            "Loss Bid",
            "You have won bid for catering service - Fork Spoon catering"
        )
        notificationList.add(obj)
        notificationList.add(obj)
        notificationList.add(obj)
        notificationList.add(obj)
        notificationList.add(obj)
        adapter.refreshItems(notificationList)
    }

    // Callback Method For Notification Onclick
    override fun onNotificationClicked(notification: NotificationDetails, position: Int) {
        Log.d(TAG, "onNotificationClicked: called $position")
    }

}