package com.smf.events.ui.notification.activenotification

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ActiveNotificationViewModel @Inject constructor(
    private val activeNotificationRepository: ActiveNotificationRepository, application: Application
) : BaseViewModel(application) {

    // 3212 - Method For Getting Notifications
    fun getNotifications(idToken: String, userId: String, isActive: Boolean) =
        liveData(Dispatchers.IO) {
            emit(activeNotificationRepository.getNotifications(idToken, userId, isActive))
        }

    // 3212 - Method For move the notifications active to old
    fun moveToOldNotification(idToken: String, notificationIds: List<Int>) =
        liveData(Dispatchers.IO) {
            emit(activeNotificationRepository.moveToOldNotification(idToken, notificationIds))
        }

}