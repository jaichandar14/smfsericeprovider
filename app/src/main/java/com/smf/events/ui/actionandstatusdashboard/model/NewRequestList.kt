package com.smf.events.ui.actionandstatusdashboard.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class NewRequestList(
    val data: Data,
    val result: Result,
    val success: Boolean
)

data class Data(
    val cost: Any,
    val costingType: Any,
    val serviceName: Any,
    val serviceProviderBidRequestDtos: List<ServiceProviderBidRequestDto>?,
    val serviceVendorBranchDto: Any
)

data class Result(
    val info: String
)

@Parcelize
data class ServiceAddressDto(
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val country: String,
    val knownVenue: Boolean,
    val state: String,
    val zipCode: String
) : Parcelable

@Parcelize
data class ServiceProviderBidRequestDto(
    val bidAcceptedDate: String,
    val bidCurrencyUnit: String,
    val bidRejected: String,
    val bidRejectedDate: String,
    val bidRequestId: Int,
    val bidRequested: String,
    val bidRequestedDate: String,
    val bidStatus: String,
    val bidSubmitted: String,
    val bidTimedOut: String,
    val biddingCutOffDate: String,
    val branchAddress: String,
    val branchName: String,
    val cost: String,
    val costingType: String,
    val currencyType: String,
    val eventDate: String,
    val eventId: Int,
    val eventName: String,
    val eventOrganizerId: Int,
    val eventServiceDescriptionId: Int,
    val isExistingUser: Boolean,
    val latestBidValue: String,
    val lostBid: String,
    val pendingForQuote: String,
    val preferredSlots: String,
    val rejectedBidComment: String,
    val rejectedBidReason: String,
    val serviceAddress: String,
    val serviceAddressDto: ServiceAddressDto,
    val serviceBranchDto: String,
    val serviceCategoryId: Int,
    val serviceDate: String,
    val serviceDone: String,
    val serviceName: String,
    val serviceProviderEmail: String,
    val serviceProviderId: Int,
    val serviceProviderName: String,
    val serviceVendorOnboardingId: Int,
    val spRegId: Int,
    val timeLeft: Double,
    val wonBid: String
) : Parcelable