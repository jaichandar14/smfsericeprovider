package com.smf.events.ui.notification

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.notification.model.NotificationCount
import retrofit2.HttpException
import javax.inject.Inject

class NotificationRepository @Inject constructor(var apiStories: ApiStories) {

    // 3212 - Api call for get Notification count
    suspend fun getNotificationCount(
        idToken: String,
        userId: String
    ): ApisResponse<NotificationCount> {
        return try {
            val getResponse = apiStories.getNotificationCount(idToken, userId)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }

}