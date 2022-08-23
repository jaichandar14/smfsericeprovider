package com.smf.events.ui.notification.activenotification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.smf.events.BR
import com.smf.events.MainActivity
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentActiveNotificationBinding
import com.smf.events.helper.AppConstants
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.notification.adapter.NotificationAdapter
import com.smf.events.ui.notification.model.NotificationDetails
import com.smf.events.ui.notification.model.NotificationParams
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ActiveNotificationFragment :
    BaseFragment<FragmentActiveNotificationBinding, ActiveNotificationViewModel>(),
    NotificationAdapter.OnNotificationClickListener {

    var TAG = "ActiveNotificationFragment"
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: NotificationAdapter
    private val notificationList = ArrayList<NotificationDetails>()
    private lateinit var dialogDisposable: Disposable

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): ActiveNotificationViewModel =
        ViewModelProvider(this, factory).get(ActiveNotificationViewModel::class.java)

    override fun getBindingVariable(): Int = BR.activenotificationViewModel

    override fun getContentView(): Int = R.layout.fragment_active_notification

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
        recyclerView = mDataBinding!!.activeNotificationRecycler
        adapter = NotificationAdapter()
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.setOnClickListener(this)
        fakeData()
        // Active To Old Swipe Listener
        swipeToOld()
        // Listener For ClearAll Button
        dialogDisposable = RxBus.listen(RxEvent.ClearAllNotification::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer ActiveNotificationFragment")
            notificationList.clear()
            adapter.refreshItems(notificationList)
        }
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

    private fun swipeToOld() {
        Log.d(TAG, "onNotificationClicked: onTabSelected swipe called")
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.d(TAG, "onSwiped: ${viewHolder.absoluteAdapterPosition}")
                val position = viewHolder.absoluteAdapterPosition
                val notificationDetail: NotificationDetails = notificationList[position]
                adapter.deleteItem(position)

                Snackbar.make(
                    recyclerView,
                    getString(R.string.moved_to_old)+" "+ notificationDetail.notificationTitle,
                    Snackbar.LENGTH_LONG
                ).setAction(
                    "Undo",
                    View.OnClickListener {
                        // adding on click listener to our action of snack bar.
                        // below line is to add our item to array list with a position.
                        adapter.addItem(position, notificationDetail)
                    }).show()
            }
        }).attachToRecyclerView(recyclerView)
    }

    // Callback Method For Notification Onclick
    override fun onNotificationClicked(notification: NotificationDetails, position: Int) {
        Log.d(TAG, "onNotificationClicked: called $position")
        val notificationParams =
            NotificationParams(fromNotification = true, backArrow = false, AppConstants.WON_BID)
        activity?.let {
            val intent = Intent(it, MainActivity::class.java)
            intent.putExtra(AppConstants.NOTIFICATION_PARAMS, notificationParams)
            it.startActivity(intent)
            it.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onViewCreated: observer ActiveNotificationFragment Destory")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

}