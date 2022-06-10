package com.smf.events.network

import com.smf.events.ui.actionandstatusdashboard.model.NewRequestList
import com.smf.events.ui.bidrejectiondialog.model.ServiceProviderBidRequestDto
import com.smf.events.ui.dashboard.model.ActionAndStatus
import com.smf.events.ui.dashboard.model.AllServices
import com.smf.events.ui.dashboard.model.Branches
import com.smf.events.ui.dashboard.model.ServiceCount
import com.smf.events.ui.emailotp.model.GetLoginInfo
import com.smf.events.ui.quotebrief.model.QuoteBrief
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import com.smf.events.ui.schedulemanagement.model.EventDates
import com.smf.events.ui.signup.model.GetUserDetails
import com.smf.events.ui.signup.model.UserDetails
import com.smf.events.ui.signup.model.UserDetailsResponse
import com.smf.events.ui.timeslot.deselectingdialog.model.ModifyDaySlotResponse
import com.smf.events.ui.timeslotmodifyexpanablelist.model.ModifyBookedServiceEvents
import com.smf.events.ui.timeslotsexpandablelist.model.BookedServiceList
import com.smf.events.ui.vieworderdetails.model.OrderDetails
import retrofit2.http.*

interface ApiStories {

    @POST("epm-no-auth/api/authentication/user-info")
    suspend fun addUserDetails(@Body userDetails: UserDetails): UserDetailsResponse

    @GET("epm-no-auth/api/authentication/user-info")
    suspend fun getUserDetails(@Query("loginName") loginName: String): GetUserDetails

    @GET("epm-user/api/app-authentication/login")
    suspend fun getLoginInfo(@Header("Authorization") idToken: String): GetLoginInfo

    @GET("epm-service/api/app-services/service-counts/{sp-reg-id}")
    suspend fun getServiceCount(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
    ): ServiceCount

    @GET("epm-service/api/app-services/services/{sp-reg-id}")
    suspend fun getAllServices(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
    ): AllServices

    @GET("epm-service/api/app-services/service-branches/{sp-reg-id}")
    suspend fun getServicesBranches(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
        @Query("serviceCategoryId") serviceCategoryId: Int,
    ): Branches

    @GET("epm-service/api/app-services/service-provider-bidding-counts/{sp-reg-id}")
    suspend fun getActionAndStatus(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
        @Query("serviceCategoryId") serviceCategoryId: Int?,
        @Query("serviceVendorOnboardingId") serviceVendorOnboardingId: Int?,
    ): ActionAndStatus

    @GET("epm-service/api/app-services/bidding-status-info/{sp-reg-id}")
    suspend fun getBidActions(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
        @Query("serviceCategoryId") serviceCategoryId: Int?,
        @Query("serviceVendorOnboardingId") serviceVendorOnBoardingId: Int?,
        @Query("bidStatus") bidStatus: List<String>,
    ): NewRequestList

    @PUT("epm-service/api/app-services/accept-bid/{bid-request-id}")
    suspend fun postQuoteDetails(
        @Header("Authorization") idToken: String,
        @Path("bid-request-id") bidRequestId: Int,
        @Body biddingQuoteDto: BiddingQuotDto,
    ): NewRequestList


    @GET("epm-service/api/app-services/order-info/{bid-request-Id}")
    suspend fun getQuoteBrief(
        @Header("Authorization") idToken: String,
        @Path("bid-request-Id") bidRequestId: Int,
    ): QuoteBrief

    @PUT("epm-service/api/app-services/bid-request-info")
    suspend fun putBidRejection(
        @Header("Authorization") idToken: String,
        @Body serviceProviderBidRequestDto: ServiceProviderBidRequestDto,
    ): NewRequestList

    //2402 - ViewOrderDetails API
    @GET("epm-service/api/app-services/order-description/{event-id}/{event-service-desc-id}")
    suspend fun getViewOrderDetails(
        @Header("Authorization") idToken: String,
        @Path("event-id") eventId: Int,
        @Path("event-service-desc-id") eventServiceDescId: Int,
    ): OrderDetails

    // 2670 - Booked Event Services API
    @GET("epm-service/api/app-services/booked-service-slots/{sp-reg-id}")
    suspend fun getBookedEventServices(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
        @Query("serviceCategoryId") serviceCategoryId: Int?,
        @Query("serviceVendorOnboardingId") serviceVendorOnBoardingId: Int?,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): BookedServiceList

    // 2622 EventDates for Calendar Api
    @GET("epm-service/api/app-services/calendar-events/{sp-reg-id}")
    suspend fun getEventDates(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
        @Query ("serviceCategoryId") serviceCategoryId:Int?,
        @Query("serviceVendorOnboardingId") serviceVendorOnboardingId:Int?,
        @Query ("fromDate") fromDate:String,
        @Query ("toDate") toDate:String,
    ): EventDates

    // 2801 - Booked Event Services API For Modify Slots
    @GET("epm-service/api/app-services/slot-availability/{sp-reg-id}")
    suspend fun getModifyBookedEventServices(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
        @Query("serviceCategoryId") serviceCategoryId: Int?,
        @Query("serviceVendorOnboardingId") serviceVendorOnBoardingId: Int?,
        @Query("isMonth") isMonth: Boolean,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): ModifyBookedServiceEvents

    // 2814 - modify-day-slot
    @PUT("epm-service/api/app-services/modify-day-slot/{sp-reg-id}")
    suspend fun getModifyDaySlot(
        @Header("Authorization") idToken: String,
        @Path("sp-reg-id") spRegId: Int,
        @Query("fromDate") fromDate: String,
        @Query("isAvailable") isAvailable: Boolean,
        @Query("modifiedSlot") modifiedSlot: String,
        @Query("serviceVendorOnboardingId") serviceVendorOnBoardingId: Int,
        @Query("toDate") toDate: String
    ): ModifyDaySlotResponse
}