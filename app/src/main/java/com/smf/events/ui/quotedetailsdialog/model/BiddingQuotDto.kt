package com.smf.events.ui.quotedetailsdialog.model

data class BiddingQuotDto(
    val bidRequestId: Int,
    val bidStatus: String?=null,
    val branchName: String?=null,
    val comment: String?=null,
    val cost: String?=null,
    val costingType: String?=null,
    val currencyType:String?=null,
    val fileContent: String?=null,
    val fileName: String?=null,
    val fileSize:String?=null,
    val fileType:String?=null,
    val latestBidValue: Int
)
