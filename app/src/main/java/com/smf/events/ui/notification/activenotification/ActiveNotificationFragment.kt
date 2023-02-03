package com.smf.events.ui.notification.activenotification

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentActiveNotificationBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.notification.NotificationActivity
import com.smf.events.ui.notification.adapter.NotificationAdapter
import com.smf.events.ui.notification.callbacks.CardViewClear
import com.smf.events.ui.notification.model.Data
import com.smf.events.ui.notification.model.Notification
import com.smf.events.ui.notification.model.NotificationDetails
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Month
import java.util.*
import javax.inject.Inject

class ActiveNotificationFragment :
    BaseFragment<FragmentActiveNotificationBinding, ActiveNotificationViewModel>(),
    NotificationAdapter.OnNotificationClickListener, Tokens.IdTokenCallBackInterface,
    CardViewClear {

    var TAG = this::class.java.name
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: NotificationAdapter
    private val notificationList = ArrayList<NotificationDetails>()
    private val notificationIdsList = ArrayList<Int>()
    private val removeNotificationIdsList = ArrayList<Int>()
    private lateinit var clearAllDisposable: Disposable
    lateinit var idToken: String
    lateinit var userId: String

    companion object {
        var clearBtnClickedPosition = 0
        var isClearAllClicked = false
    }

    @Inject
    lateinit var tokens: Tokens

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): ActiveNotificationViewModel =
        ViewModelProvider(this, factory)[ActiveNotificationViewModel::class.java]

    override fun getBindingVariable(): Int = BR.activenotificationViewModel

    override fun getContentView(): Int = R.layout.fragment_active_notification

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Local Variables
        setIdTokenAndSpRegId()
        // Initialize IdTokenCallBackInterface
        tokens.setCallBackInterface(this)
        // Listener For ClearAll Button
        clearAllDisposable = RxBus.listen(RxEvent.ClearAllNotification::class.java).subscribe {
            isClearAllClicked = true
            removeNotificationIdsList.clear()
            removeNotificationIdsList.addAll(notificationIdsList)
            // Check IdToken Validity
            idTokenValidation(getString(R.string.delete))
        }
        // Initializing RecyclerView
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        // Initialize RecyclerView Active
        recyclerView = mDataBinding!!.activeNotificationRecycler
        adapter = NotificationAdapter(AppConstants.ACTIVE)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.setOnClickListener(this)
        // Clear Listener
        adapter.setOnClickClearListener(this)
        // Check IdToken Validity
        idTokenValidation(getString(R.string.notification))
    }

    private fun idTokenValidation(caller: String) {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp, caller, idToken
        )
    }

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        view?.let {
            withContext(Dispatchers.Main) {
                when (caller) {
                    getString(R.string.notification) -> getNotifications(idToken, userId)
                    getString(R.string.delete) -> moveToOldNotification(idToken)
                }
            }
        }
    }

    private fun getNotifications(
        idToken: String, userId: String
    ) {
        // Showing Progressbar
        showProgress()
        getViewModel().getNotifications(idToken, userId, true)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        Log.d(TAG, "getNotifications: $apiResponse")
                        createNotificationList(apiResponse)
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "check token result: ${apiResponse.message}")
                        showToastMessage(
                            apiResponse.message,
                            Snackbar.LENGTH_LONG,
                            AppConstants.PLAIN_SNACK_BAR
                        )
                        hideProgress()
                    }
                    is ApisResponse.InternetError -> {
                        (requireActivity() as NotificationActivity).showInternetDialog(apiResponse.message)
                        hideProgress()
                    }
                    else -> {}
                }
            })
    }

    private fun createNotificationList(apiResponse: ApisResponse.Success<Notification>) {
        notificationList.clear()
        if (apiResponse.response.data.isNotEmpty()) {
            mDataBinding?.noRecordsText?.visibility = View.GONE
            apiResponse.response.data.forEach {
                val time = getDateAndTime(it)
                val notification = NotificationDetails(
                    it.notificationId,
                    time,
                    it.notificationType,
                    it.notificationTitle,
                    it.notificationContent
                )
                notificationList.add(notification)
                // Add Notification Ids
                notificationIdsList.add(it.notificationId)
            }
            adapter.refreshItems(notificationList)
        } else {
            mDataBinding?.noRecordsText?.visibility = View.VISIBLE
        }
        // Hiding Progressbar
        hideProgress()
    }

    private fun getDateAndTime(data: Data): String {
        val date = data.formatedCreatedDate.substring(0, 2)
        val monthCount = data.formatedCreatedDate.substring(3, 4)
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3).let { month ->
            month.substring(0, 1) + month.substring(1, 2)
                .lowercase(Locale.getDefault()) + month.substring(2, 3)
                .lowercase(Locale.getDefault())
        }
        val time = data.formatedCreatedDate.split(",")[1].substring(0, 5)
        val updatedTime =
            if ("${time.substring(0, 2)}.${time.substring(3, 5)}".toFloat() < 12.01.toFloat()) {
                "${data.formatedCreatedDate.split(",")[1].substring(0, 5)} Am"
            } else {
                "${data.formatedCreatedDate.split(",")[1].substring(0, 5)} Pm"
            }

        return "$month $date - $updatedTime"
    }

    // Callback Method For Notification Onclick
    override fun onNotificationClicked(notification: NotificationDetails, position: Int) {
        Log.d(TAG, "onNotificationClicked: called $position")
//        val notificationParams =
//            NotificationParams(fromNotification = true, backArrow = false, AppConstants.WON_BID)
//        activity?.let {
//            val intent = Intent(it, MainActivity::class.java)
//            intent.putExtra(AppConstants.NOTIFICATION_PARAMS, notificationParams)
//            it.startActivity(intent)
//            it.finish()
//        }
    }

    // Setting IdToken, SpRegId And RollId
    private fun setIdTokenAndSpRegId() {
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        userId = "${sharedPreference.getString(SharedPreference.USER_ID)}"
    }

    private fun showProgress() {
        mDataBinding?.progressBar?.visibility = View.VISIBLE
        mDataBinding?.activeNotificationRecycler?.visibility = View.INVISIBLE
    }

    private fun hideProgress() {
        mDataBinding?.progressBar?.visibility = View.GONE
        mDataBinding?.activeNotificationRecycler?.visibility = View.VISIBLE
    }

    override fun onClearButtonClicked(position: Int) {
        Log.d(TAG, "clearButtonClicked: $position")
        clearBtnClickedPosition = position
        val notificationId = notificationIdsList[position]
        removeNotificationIdsList.clear()
        removeNotificationIdsList.add(notificationId)
        // Check IdToken Validity
        idTokenValidation(getString(R.string.delete))
    }

    // 3212 - Method For move the notifications active to old page
    private fun moveToOldNotification(idToken: String) {
        getViewModel().moveToOldNotification(idToken, removeNotificationIdsList)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        Log.d(TAG, "moveToOldNotification: $apiResponse")
                        if (isClearAllClicked) {
                            notificationList.clear()
                            adapter.refreshItems(notificationList)
                            isClearAllClicked = false
                        } else {
                            notificationList.removeAt(clearBtnClickedPosition)
                            notificationIdsList.removeAt(clearBtnClickedPosition)
                            adapter.refreshItems(notificationList)
                        }
                        // Update Notification Count
                        RxBus.publish(RxEvent.UpdateNotificationCount(AppConstants.ACTIVE))
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(
                            TAG, "check token result moveToOldNotification: ${apiResponse.message}"
                        )
                    }
                    is ApisResponse.InternetError -> {
                        (requireActivity() as NotificationActivity)
                            .showInternetDialog(apiResponse.message)
                        hideProgress()
                    }
                    else -> {}
                }
            })
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onViewCreated: observer ActiveNotificationFragment stop")
        if (clearAllDisposable.isDisposed.not()) clearAllDisposable.dispose()
    }

}