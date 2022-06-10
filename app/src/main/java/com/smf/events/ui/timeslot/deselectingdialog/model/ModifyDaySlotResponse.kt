package com.smf.events.ui.timeslot.deselectingdialog.model

data class ModifyDaySlotResponse(
    val data: Data,
    val result: Result,
    val success: Boolean
)

data class Data(
    val docId: Any,
    val key: Int,
    val status: String,
    val statusCode: Int
)

data class Result(
    val info: String
)