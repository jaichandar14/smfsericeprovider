package com.smf.events.ui.notification.activenotification

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.notification.model.MoveOldResponse
import com.smf.events.ui.notification.model.Notification
import retrofit2.HttpException
import javax.inject.Inject

class ActiveNotificationRepository @Inject constructor(var apiStories: ApiStories) {

    // 3212 - Api call for Active Notifications
    suspend fun getNotifications(
        idToken: String,
        userId: String,
        isActive: Boolean
    ): ApisResponse<Notification> {
        return try {
            val getResponse = apiStories.getNotifications(idToken, userId, isActive)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }

    // 3212 - Api call for move the notifications active to old
    suspend fun moveToOldNotification(
        idToken: String,
        notificationIds: List<Int>
    ): ApisResponse<MoveOldResponse> {
        return try {
            val getResponse = apiStories.moveToOldNotification(idToken, notificationIds)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }

}