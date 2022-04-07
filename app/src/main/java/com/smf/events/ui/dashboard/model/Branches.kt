package com.smf.events.ui.dashboard.model

import com.google.gson.annotations.SerializedName


data class Branches(
    val success: Boolean,
    @SerializedName("data")
    val datas:List<DatasNew>,
    val result: Result
)


data class DatasNew(val spRegId:Int,
                 val status :String,
                 val serviceVendorOnboardingId:Int,
                 val serviceVendorMetadataDto:String,
                 val serviceVendorBranchDto:String,
                 val branchName:String)

data class Result(val info: String)

