package com.smf.events.ui.notification.activenotification

import android.app.Application
import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseViewModel
import com.smf.events.helper.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class ActiveNotificationViewModel @Inject constructor(
    private val activeNotificationRepository: ActiveNotificationRepository,
    application: Application
) : BaseViewModel(application) {

    // 3212 - Method For Getting Notifications
    fun getNotifications(idToken: String, userId: String, isActive: Boolean) =
        liveData(Dispatchers.IO) {
            try {
                emit(activeNotificationRepository.getNotifications(idToken, userId, isActive))
            } catch (e: Exception) {
                Log.d("TAG", "getBookedEventServices mody: $e")
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getBookedEventServices when mody called: $e")
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                }
            }
        }

    // 3212 - Method For move the notifications active to old
    fun moveToOldNotification(idToken: String, notificationIds: List<Int>) =
        liveData(Dispatchers.IO) {
            try {
                emit(activeNotificationRepository.moveToOldNotification(idToken, notificationIds))
            } catch (e: Exception) {
                Log.d("TAG", "getBookedEventServices mody: $e")
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getBookedEventServices when mody called: $e")
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                }
            }
        }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun internetError(exception: String)
    }

}