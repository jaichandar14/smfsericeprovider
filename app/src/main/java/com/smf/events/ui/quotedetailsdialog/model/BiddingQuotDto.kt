package com.smf.events.ui.quotedetailsdialog.model

data class BiddingQuotDto(
    val bidRequestId: Int,
    val bidStatus: String? = null,
    val branchName: String? = null,
    val comment: String? = null,
    val cost: String? = null,
    val costingType: String? = null,
    val currencyType: String? = null,
    var fileContent: String? = null,
    var fileName: String? = null,
    var fileSize: String? = null,
    val fileType: String? = null,
    val latestBidValue: Int
)
