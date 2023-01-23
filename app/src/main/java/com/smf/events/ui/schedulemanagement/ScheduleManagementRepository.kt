package com.smf.events.ui.schedulemanagement

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.AllServices
import com.smf.events.ui.dashboard.model.Branches
import com.smf.events.ui.schedulemanagement.model.BusinessValidity
import com.smf.events.ui.schedulemanagement.model.EventDates
import com.smf.events.ui.timeslotmodifyexpanablelist.model.ModifyBookedServiceEvents
import com.smf.events.ui.timeslotsexpandablelist.model.BookedServiceList
import javax.inject.Inject

//2458
class ScheduleManagementRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    // 2458 Get Api call Method for All Services
    suspend fun getAllServices(idToken: String, spRegId: Int): ApisResponse<AllServices> {
        return safeApiCall { apiStories.getAllServices(idToken, spRegId) }
    }

    // 2458 Get Api call Method for Branches
    suspend fun getServicesBranches(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int,
    ): ApisResponse<Branches> {
        return safeApiCall { apiStories.getServicesBranches(idToken, spRegId, serviceCategoryId) }
    }

    // 2670 - Method For Get Booked Event Services
    suspend fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String
    ): ApisResponse<BookedServiceList> {
        return safeApiCall {
            apiStories.getBookedEventServices(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnBoardingId,
                fromDate,
                toDate
            )
        }
    }

    // 2622 EventDates for Calendar Api
    suspend fun getEventDates(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
        fromDate: String,
        toDate: String,
    ): ApisResponse<EventDates> {
        return safeApiCall {
            apiStories.getEventDates(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnboardingId,
                fromDate,
                toDate
            )
        }
    }

    // 2801 - Booked Event Services API For Modify Slots
    suspend fun getModifyBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        isMonth: Boolean,
        fromDate: String,
        toDate: String
    ): ApisResponse<ModifyBookedServiceEvents> {
        return safeApiCall {
            apiStories.getModifyBookedEventServices(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnBoardingId,
                isMonth,
                fromDate,
                toDate
            )
        }
    }

    // 2458 Get Api call Method for All Services
    suspend fun getBusinessValiditiy(
        idToken: String,
        spRegId: Int
    ): ApisResponse<BusinessValidity> {
        return safeApiCall { apiStories.getBusinessValiditiy(idToken, spRegId) }
    }
}