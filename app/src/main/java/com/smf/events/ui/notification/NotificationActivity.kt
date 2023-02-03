package com.smf.events.ui.notification

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseActivity
import com.smf.events.databinding.ActivityNotificationBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.notification.activenotification.ActiveNotificationFragment
import com.smf.events.ui.notification.oldnotification.OldNotificationFragment
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationActivity :
    BaseActivity<ActivityNotificationBinding, NotificationViewModel>(),
    Tokens.IdTokenCallBackInterface {

    var TAG = this::class.java.name
    lateinit var tabLayout: TabLayout
    var tabSelectedPosition: Int = 0
    var activeNotificationCount: Int = 0
    var status: String = ""
    lateinit var idToken: String
    lateinit var userId: String
    private lateinit var internetStatusDisposable: Disposable
    private lateinit var dialogDisposable: Disposable

    @Inject
    lateinit var tokens: Tokens

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getContentView(): Int = R.layout.activity_notification

    override fun getViewModel(): NotificationViewModel =
        ViewModelProvider(this, factory)[NotificationViewModel::class.java]

    override fun getBindingVariable(): Int = BR.notificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        // Set Status bar
        setStatusBarColor()
        // Initialize Tabview
        tabLayout = mViewDataBinding!!.tabLayout
        // Initialize Local Variables
        setIdTokenAndSpRegId()
        // Back Key Listener
        backKeyListener()
        // Initialize IdTokenCallBackInterface
        tokens.setCallBackInterface(this)
        // Check IdToken Validity
        idTokenValidation(getString(R.string.notification_count))
        // UI Initialization
        uiInitialization()

        observers()
    }

    private fun observers() {
        // Observe & Set Active notification count
        getViewModel().getActiveNotificationCount.observe(this, Observer {
            tabLayout.getTabAt(0)?.text = "${AppConstants.ACTIVE}(${it})"
        })
        // Observe & Set Old notification count
        getViewModel().getOldNotificationCount.observe(this, Observer {
            tabLayout.getTabAt(1)?.text = "${AppConstants.OLD}(${it})"
        })
    }

    override fun onResume() {
        super.onResume()
        // Listener For Internet Connectivity
        internetStatusDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer notification activity")
            // Check IdToken Validity
            idTokenValidation(getString(R.string.notification_count))
            updateNotificationUI()
        }
        // Listener For ClearAll Button
        dialogDisposable = RxBus.listen(RxEvent.UpdateNotificationCount::class.java).subscribe {
            // Check IdToken Validity
            idTokenValidation(getString(R.string.notification_count))
        }
    }

    private fun idTokenValidation(caller: String) {
        tokens.checkTokenExpiry(
            applicationContext as SMFApp,
            caller, idToken
        )
    }

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Dispatchers.Main) {
            when (caller) {
                getString(R.string.notification_count) -> getNotificationCount(idToken, userId)
            }
        }
    }

    private fun getNotificationCount(
        idToken: String,
        userId: String
    ) {
        getViewModel().getNotificationCount(idToken, userId)
            .observe(this, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        Log.d(TAG, "getNotifications count: $apiResponse")
                        // Active notification count
                        getViewModel()
                            .setActiveNotificationCount(apiResponse.response.data.activeCounts)
                        // Old notification count
                        getViewModel()
                            .setOldNotificationCount(apiResponse.response.data.oldCounts)
                        // Update clearAllButton Visibility
                        activeNotificationCount = apiResponse.response.data.activeCounts
                        clearAllBtnVisibility()
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "check token result: ${apiResponse.message}")
                        showToastMessage(
                            apiResponse.message,
                            Snackbar.LENGTH_LONG,
                            AppConstants.PLAIN_SNACK_BAR
                        )
                    }
                    is ApisResponse.InternetError -> {
                        showInternetDialog(apiResponse.message)
                    }
                    else -> {}
                }
            })
    }

    private fun backKeyListener() {
        // restrict user back button
        onBackPressedDispatcher.addCallback(this) {
            moveToDashBoard()
        }
    }

    private fun uiInitialization() {
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
        if (tabSelectedPosition == 0 && activeNotificationCount != 0) {
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

    // Setting IdToken, SpRegId And RollId
    private fun setIdTokenAndSpRegId() {
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        userId = "${sharedPreference.getString(SharedPreference.USER_ID)}"
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onViewCreated: observer NotificationActivity Destroy")
        if (dialogDisposable.isDisposed.not()) dialogDisposable.dispose()
        if (internetStatusDisposable.isDisposed.not()) internetStatusDisposable.dispose()
    }
}