package com.smf.events.ui.dashboard.model

data class ActionAndStatusCount(
    val bidRequestedCount: Int, val bidSubmittedCount: Int,
    val bidRejectedCount: Int, val pendingForQuoteCount: Int,
    val wonBidCount: Int, val lostBidCount: Int,
    val bidTimedOutCount: Int, val serviceDoneCount: Int,
    val statusCount: Int, val actionCount: Int, val serviceInProgressCount: Int,
)


