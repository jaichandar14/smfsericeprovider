package com.smf.events.ui.actiondetails.model

data class ActionDetails(
    val bidRequestId: Int,
    val serviceCategoryId: Int,
    val eventId: Int,
    val eventDate: String,
    val eventName: String,
    val serviceName: String,
    val serviceDate: String,
    val bidRequestedDate: String,
    val biddingCutOffDate: String,
    val costingType: String,
    val cost: String?,
    val latestBidValue: String?,
    val bidStatus: String,
    val isExistingUser: Boolean,
    val eventServiceDescriptionId: Int,
    val branchName: String,
    val timeLeft: Double
)
