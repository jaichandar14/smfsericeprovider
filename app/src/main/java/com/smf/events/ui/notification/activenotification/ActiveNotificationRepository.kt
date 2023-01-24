package com.smf.events.ui.notification.activenotification

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.notification.model.MoveOldResponse
import com.smf.events.ui.notification.model.Notification
import javax.inject.Inject

class ActiveNotificationRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    // 3212 - Api call for Active Notifications
    suspend fun getNotifications(
        idToken: String,
        userId: String,
        isActive: Boolean
    ): ApisResponse<Notification> {
        return safeApiCall { apiStories.getNotifications(idToken, userId, isActive) }
    }

    // 3212 - Api call for move the notifications active to old
    suspend fun moveToOldNotification(
        idToken: String,
        notificationIds: List<Int>
    ): ApisResponse<MoveOldResponse> {
        return safeApiCall { apiStories.moveToOldNotification(idToken, notificationIds) }
    }

}