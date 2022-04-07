package com.smf.events.ui.quotebrief.model

import com.google.gson.annotations.SerializedName
import com.smf.events.ui.actionandstatusdashboard.model.Data
import com.smf.events.ui.actionandstatusdashboard.model.Result

data class QuoteBrief(
    val success: Boolean,
    @SerializedName("data")
    val data: Datas,
    val result: Result,



)

data class Datas(val bidRequestId: Int,
                val serviceProviderId: String,
                val serviceCategoryId: Int,
    val spRegId: Int,
                val serviceProviderName: String,
val serviceVendorOnboardingId: Int,
val bidCurrencyUnit: String,
val eventOrganizerId: String,
val eventId: Int,
val eventDate: String,
val eventName: String,
val serviceName: String,
val serviceDate: String,
val bidRequestedDate: String,
val biddingCutOffDate: String,
val costingType: String,
val cost: String,
val bidStatus: String,
val latestBidValue: String,
val currencyType: String,
val rejectedBidReason:String,
val rejectedBidComment: String,
val bidAcceptedDate: String,
val bidRejectedDate: String,
val isExistingUser: String,
val serviceProviderEmail: String,
                val eventServiceDescriptionId: Int,
val branchName: String,
val branchAddress: String,
val bidRequested: String,
val bidSubmitted: String,
val bidRejected: String,
val pendingForQuote: String,
val wonBid: String,
val lostBid: String,
val bidTimedOut: String,
val serviceDone: String,
val timeLeft: Double,
                 val serviceAddressDto:ServiceAddressDto
)
data class ServiceAddressDto(
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val country: String,
    val knownVenue: Boolean,
    val state: String,
    val zipCode: String
)



data class Result(
    val info: String
)

