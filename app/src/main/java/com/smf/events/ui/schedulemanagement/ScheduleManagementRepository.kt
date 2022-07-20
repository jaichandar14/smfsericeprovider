package com.smf.events.ui.schedulemanagement

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.AllServices
import com.smf.events.ui.dashboard.model.Branches
import com.smf.events.ui.schedulemanagement.model.BusinessValidity
import com.smf.events.ui.schedulemanagement.model.EventDates
import com.smf.events.ui.timeslotmodifyexpanablelist.model.ModifyBookedServiceEvents
import com.smf.events.ui.timeslotsexpandablelist.model.BookedServiceList
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

    // 2670 - Method For Get Booked Event Services
    suspend fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String
    ): ApisResponse<BookedServiceList> {
        return try {
            val getResponse = apiStories.getBookedEventServices(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnBoardingId,
                fromDate,
                toDate
            )
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
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
        return try {
            val getResponse = apiStories.getEventDates(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnboardingId,
                fromDate,
                toDate
            )
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
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
        return try {
            val getResponse = apiStories.getModifyBookedEventServices(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnBoardingId,
                isMonth,
                fromDate,
                toDate
            )
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }


    // 2458 Get Api call Method for All Services
    suspend fun getBusinessValiditiy(idToken: String, spRegId: Int): ApisResponse<BusinessValidity> {
        return try {
            val getResponse = apiStories.getBusinessValiditiy(idToken, spRegId)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}