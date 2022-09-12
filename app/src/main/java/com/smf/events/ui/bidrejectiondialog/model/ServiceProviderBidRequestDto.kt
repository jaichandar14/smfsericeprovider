package com.smf.events.ui.bidrejectiondialog.model

data class ServiceProviderBidRequestDto(
    val bidRequestId: Int,
    val rejectedBidComment: String?,
    val rejectedBidReason: String?
)

//                                        val bidAcceptedDate:String,

//val bidRequestId: Int,
//val bidRequested: Int,
//val bidRequestedDate: String,
//val bidStatus: String,
//val bidSubmitted: Int,
//val bidTimedOut: Int,
//val biddingCutOffDate: String,
//val branchAddress: String,
//val branchName: String,
//val cost: String,
//val costingType:String,
//val currencyTyp: String,
//val eventDate: String,
//val eventId: Int,
//val eventName: String,
//val eventOrganizerId: String,
//val eventServiceDescriptionId: Int,
//val existingUser: Boolean,
//val isExistingUser: Boolean,
//val latestBidValue: Int,
//val lostBid: Int,
//val pendingForQuote: Int,
//val preferredSlots:Array<String>,
//val rejectedBidComment: String,
//val rejectedBidReason: String,
//val serviceAddress: String,
//val serviceAddressDto:ServiceAddressDto,
//val serviceBranchDto: ServiceBranchDto ,
//val serviceCategoryId: Int,
//val serviceDate: String,
//val serviceDone: Int,
//val serviceName: String,
//val serviceProviderEmail:String,
//val serviceProviderId: String,
//val serviceProviderName:String,
//val serviceVendorOnboardingId: Int,
//val spRegId: Int,
//val timeLeft: Int,
//val wonBid: Int
//)
//
//
//
//data class ServiceAddressDto(
//    val addressLine1: String,
//val addressLine2: String,
//val city: String,
//val country: String,
//val knownVenue: Boolean,
//val state: String,
//val zipCode: String
//)
//
//data class ServiceBranchDto(
//    val addressLine1: String,
//val addressLine2: String,
//val branchName: String,
//val city: String,
//val country: String,
//val languagesKnown: Array<String>,
//val phoneNumber: String,
//val state: String,
//val zipCode:String
//     )