package com.smf.events.ui.notification.oldnotification

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class OldNotificationViewModel @Inject constructor(
    private val oldNotificationRepository: OldNotificationRepository,
    application: Application
) : BaseViewModel(application) {

    // 3212 - Method For Getting Notifications
    fun getNotifications(idToken: String, userId: String, isActive: Boolean) =
        liveData(Dispatchers.IO) {
            emit(oldNotificationRepository.getNotifications(idToken, userId, isActive))
        }

}