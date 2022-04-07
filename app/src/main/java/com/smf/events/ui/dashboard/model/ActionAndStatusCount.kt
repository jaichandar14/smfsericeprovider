package com.smf.events.ui.dashboard.model

data class ActionAndStatusCount(val bidRequestedActionsCount:Int,val bidSubmittedStatusCount:Int,
val bidSubmittedActionCount:Int,val bidRejectedStatusCount:Int,
val bidRejectedActionCount:Int,val pendingForQuoteActionCount: Int,
val wonBidStatusCount:Int,val lostBidStatusCount:Int,val bidTimedOutStatusCount: Int,
val serviceDoneStatusCount: Int,val statusCount:Int,val actionCount: Int)


