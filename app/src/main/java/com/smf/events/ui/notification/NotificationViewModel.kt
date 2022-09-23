package com.smf.events.ui.notification

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseViewModel
import com.smf.events.helper.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    application: Application,
) : BaseViewModel(application) {

    private var activeNotificationCount = MutableLiveData<Int>()
    fun setActiveNotificationCount(activeNotificationCount: Int) {
        this.activeNotificationCount.postValue(activeNotificationCount)
    }

    val getActiveNotificationCount: LiveData<Int> = activeNotificationCount

    private var oldNotificationCount = MutableLiveData<Int>()
    fun setOldNotificationCount(oldNotificationCount: Int) {
        this.oldNotificationCount.postValue(oldNotificationCount)
    }

    val getOldNotificationCount: LiveData<Int> = oldNotificationCount

    // 3212 - Method For move the notifications active to old
    fun getNotificationCount(idToken: String, userId: String) =
        liveData(Dispatchers.IO) {
            try {
                emit(notificationRepository.getNotificationCount(idToken, userId))
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