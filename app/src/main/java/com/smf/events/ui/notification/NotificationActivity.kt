package com.smf.events.ui.notification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.smf.events.BR
import com.smf.events.MainActivity
import com.smf.events.R
import com.smf.events.base.BaseActivity
import com.smf.events.databinding.ActivityNotificationBinding
import com.smf.events.helper.AppConstants
import com.smf.events.helper.ApplicationUtils
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.notification.activenotification.ActiveNotificationFragment
import com.smf.events.ui.notification.oldnotification.OldNotificationFragment
import dagger.android.AndroidInjection
import javax.inject.Inject

class NotificationActivity :
    BaseActivity<ActivityNotificationBinding, NotificationViewModel>() {

    var TAG = "NotificationActivity"
    lateinit var tabLayout: TabLayout
    var tabSelectedPosition: Int = 0
    var status: String = ""

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getContentView(): Int = R.layout.activity_notification

    override fun getViewModel(): NotificationViewModel =
        ViewModelProvider(this, factory).get(NotificationViewModel::class.java)

    override fun getBindingVariable(): Int = BR.notificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        backKeyListener()
        // UI Initialization
        uiInitialization()
    }

    private fun backKeyListener() {
        // restrict user back button
        onBackPressedDispatcher.addCallback(this) {
            moveToDashBoard()
        }
    }

    private fun uiInitialization() {
        // Initialize Tabview
        tabLayout = mViewDataBinding!!.tabLayout
        // Setting Tabview
        setTabView()
        // Initial Tab Position
        updateNotificationUI()
        // 3103 - BackArrow ClickListener
        backArrowClickListener()
        // 3103 - ClearAll ClickListener
        clearAllBtnClickListener()
    }

    private fun updateNotificationUI() {
        // 3103 - Visibility for Clear All Button
        clearAllBtnVisibility()
        status = if (tabSelectedPosition == 0) {
            AppConstants.ACTIVE
        } else {
            AppConstants.OLD
        }
        val frg = when (tabSelectedPosition) {
            0 -> {
                ActiveNotificationFragment()
            }
            1 -> {
                OldNotificationFragment()
            }
            else -> {
                ActiveNotificationFragment()
            }
        }
        val manager: FragmentManager =
            supportFragmentManager //create an instance of fragment manager
        val transaction: FragmentTransaction =
            manager.beginTransaction() //create an instance of Fragment-transaction
        transaction.replace(R.id.notification_fragment, frg, status)
        transaction.commit()
    }

    private fun clearAllBtnVisibility() {
        if (tabSelectedPosition == 0) {
            mViewDataBinding!!.closeAllBtn.visibility = View.VISIBLE
            mViewDataBinding!!.clearAllText.visibility = View.VISIBLE
        } else {
            mViewDataBinding!!.closeAllBtn.visibility = View.GONE
            mViewDataBinding!!.clearAllText.visibility = View.GONE
        }
    }

    private fun backArrowClickListener() {
        mViewDataBinding?.backArrow?.setOnClickListener {
            moveToDashBoard()
        }
    }

    private fun moveToDashBoard() {
        // Update Status for back arrow
        ApplicationUtils.backArrowNotification = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearAllBtnClickListener() {
        mViewDataBinding?.closeAllBtn?.setOnClickListener {
            RxBus.publish(RxEvent.ClearAllNotification(true))
        }
    }

    private fun setTabView() {
        tabLayout.addTab(tabLayout.newTab().setText(AppConstants.ACTIVE))
        tabLayout.addTab(tabLayout.newTab().setText(AppConstants.OLD))
        setTabClickListener()
    }

    private fun setTabClickListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabSelectedPosition = tab!!.position
                // Update Tab Position
                updateNotificationUI()
                Log.d(TAG, "onNotificationClicked: onTabSelected end $tabSelectedPosition")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

}