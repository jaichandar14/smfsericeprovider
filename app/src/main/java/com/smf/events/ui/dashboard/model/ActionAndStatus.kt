package com.smf.events.ui.dashboard.model

import com.google.gson.annotations.SerializedName


data class ActionAndStatus(
    val success: Boolean,
    @SerializedName("data")
    val actionandStatus:DataActionAndStatus,
    val result: Results
)
data class DataActionAndStatus(val bidRequestedActionsCount:Int, val bidSubmittedStatusCount:Int,
                               val bidSubmittedActionCount:Int, val bidRejectedStatusCount:Int,
                               val bidRejectedActionCount:Int, val pendingForQuoteActionCount: Int,
                               val wonBidStatusCount:Int, val lostBidStatusCount:Int, val bidTimedOutStatusCount: Int,
                               val serviceDoneStatusCount: Int, val statusCount:Int, val actionCount: Int)



data class Results(val info: String)

data class BranchDatas(val branchName:String, val branchId:Int)