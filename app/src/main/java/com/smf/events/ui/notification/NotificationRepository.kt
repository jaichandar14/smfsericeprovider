package com.smf.events.ui.notification

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.notification.model.NotificationCount
import javax.inject.Inject

class NotificationRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    // 3212 - Api call for get Notification count
    suspend fun getNotificationCount(
        idToken: String,
        userId: String
    ): ApisResponse<NotificationCount> {
        return safeApiCall { apiStories.getNotificationCount(idToken, userId) }
    }
}