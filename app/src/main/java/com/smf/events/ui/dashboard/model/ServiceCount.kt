package com.smf.events.ui.dashboard.model


data class ServiceCount(
    val success: Boolean,
    var data: Datas,
    val result: Result
)


data class Datas(
    val activeServiceCount: Int,
    val approvalPendingServiceCount: Int,
    val draftServiceCount: Int,
    val inactiveServiceCount: Int,
    val rejectedServiceCount: Int,
    val serviceVendorOnboardDtos: Int
)
