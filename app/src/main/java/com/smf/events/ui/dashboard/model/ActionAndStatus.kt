package com.smf.events.ui.dashboard.model

import com.google.gson.annotations.SerializedName

data class ActionAndStatus(
    val success: Boolean,
    @SerializedName("data")
    val actionandStatus: DataActionAndStatus,
    val result: Results,
)

data class DataActionAndStatus(
    val bidRequestedCount: Int, val bidSubmittedCount: Int,
    val bidRejectedCount: Int, val pendingForQuoteCount: Int,
    val wonBidCount: Int, val lostBidCount: Int,
    val bidTimedOutCount: Int, val serviceDoneCount: Int,
    val statusCount: Int, val actionCount: Int,
)

data class Results(val info: String)

data class BranchDatas(val branchName: String, val branchId: Int)