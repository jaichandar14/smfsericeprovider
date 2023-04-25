package com.smf.events.ui.notification.oldnotification

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
import com.smf.events.databinding.FragmentOldNotificationBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.ui.notification.NotificationActivity
import com.smf.events.ui.notification.adapter.NotificationAdapter
import com.smf.events.ui.notification.model.Data
import com.smf.events.ui.notification.model.Notification
import com.smf.events.ui.notification.model.NotificationDetails
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Month
import java.util.*
import javax.inject.Inject

class OldNotificationFragment :
    BaseFragment<FragmentOldNotificationBinding, OldNotificationViewModel>(),
    NotificationAdapter.OnNotificationClickListener, Tokens.IdTokenCallBackInterface {

    var TAG = this::class.java.name
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: NotificationAdapter
    private val notificationList = ArrayList<NotificationDetails>()
    lateinit var idToken: String
    lateinit var userId: String

    @Inject
    lateinit var tokens: Tokens

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): OldNotificationViewModel =
        ViewModelProvider(this, factory)[OldNotificationViewModel::class.java]

    override fun getBindingVariable(): Int = BR.oldnotificationViewModel

    override fun getContentView(): Int = R.layout.fragment_old_notification

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
        // Initializing RecyclerView
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        // Initialize RecyclerView Active
        recyclerView = mDataBinding!!.oldNotificationRecycler
        adapter = NotificationAdapter(AppConstants.OLD)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.setOnClickListener(this)
        // Check IdToken Validity
        idTokenValidation()
    }

    private fun idTokenValidation() {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            getString(R.string.notification), idToken
        )
    }

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        view?.let {
            withContext(Dispatchers.Main) {
                when (caller) {
                    getString(R.string.notification) -> getNotifications(idToken, userId)
                }
            }
        }
    }

    private fun getNotifications(
        idToken: String,
        userId: String
    ) {
        // Showing Progressbar
        getViewModel().showProgress()
        getViewModel().getNotifications(idToken, userId, false)
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
                    }
                    is ApisResponse.InternetError -> {
                        (requireActivity() as NotificationActivity)
                            .showInternetDialog(apiResponse.message)
                    }
                    else -> {}
                }
                getViewModel().hideProgress()
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
            }
            adapter.refreshItems(notificationList)
        } else {
            mDataBinding?.noRecordsText?.visibility = View.VISIBLE
        }
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
    }

    // Setting IdToken, SpRegId And RollId
    private fun setIdTokenAndSpRegId() {
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        userId = "${sharedPreference.getString(SharedPreference.USER_ID)}"
    }

}