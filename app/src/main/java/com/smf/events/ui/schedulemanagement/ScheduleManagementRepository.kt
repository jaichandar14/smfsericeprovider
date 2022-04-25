package com.smf.events.ui.schedulemanagement

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.AllServices
import com.smf.events.ui.dashboard.model.Branches
import retrofit2.HttpException
import javax.inject.Inject

//2458
class ScheduleManagementRepository @Inject constructor(var apiStories: ApiStories) {

    // 2458 Get Api call Method for All Services
    suspend fun getAllServices(idToken: String, spRegId: Int): ApisResponse<AllServices> {
        return try {
            val getResponse = apiStories.getAllServices(idToken, spRegId)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }

    // 2458 Get Api call Method for Branches
    suspend fun getServicesBranches(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int,
    ): ApisResponse<Branches> {
        return try {
            val getResponse =
                apiStories.getServicesBranches(idToken, spRegId, serviceCategoryId)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}