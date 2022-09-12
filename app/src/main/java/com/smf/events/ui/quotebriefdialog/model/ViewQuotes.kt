package com.smf.events.ui.quotebriefdialog.model

import com.google.gson.annotations.SerializedName
import com.smf.events.ui.actionandstatusdashboard.model.Result

data class ViewQuotes(
    val success: Boolean,
    @SerializedName("data")
    val data: Datas,
    val result: Result,
)

data class Datas(
    val fileName: String,
    val fileContent: String,
    val fileType: String,
    val fileSize: String,
    val latestBidValue: String,
    val comment: String?,
    val costingType: String,
    val cost: String,
    val currencyType: String?
)

data class Result(
    val info: String
)
