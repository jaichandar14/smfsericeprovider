package com.smf.events.ui.dashboard

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.AllServices
import com.smf.events.ui.dashboard.model.Branches
import com.smf.events.ui.dashboard.model.ServiceCount
import com.smf.events.ui.notification.model.NotificationCount
import javax.inject.Inject

class DashBoardRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    suspend fun getServiceCount(idToken: String, spRegId: Int): ApisResponse<ServiceCount> {
        return safeApiCall { apiStories.getServiceCount(idToken, spRegId) }
    }

    suspend fun getAllServices(idToken: String, spRegId: Int): ApisResponse<AllServices> {
        return safeApiCall { apiStories.getAllServices(idToken, spRegId) }
    }

    suspend fun getServicesBranches(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int
    ): ApisResponse<Branches> {
        return safeApiCall { apiStories.getServicesBranches(idToken, spRegId, serviceCategoryId) }
    }

    // 3218 - Api call for get Notification count
    suspend fun getNotificationCount(
        idToken: String,
        userId: String
    ): ApisResponse<NotificationCount> {
        return safeApiCall { apiStories.getNotificationCount(idToken, userId) }
    }
}