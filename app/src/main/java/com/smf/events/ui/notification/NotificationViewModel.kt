package com.smf.events.ui.notification

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
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
    fun getNotificationCount(idToken: String, userId: String) = liveData(Dispatchers.IO) {
        emit(notificationRepository.getNotificationCount(idToken, userId))
    }

}