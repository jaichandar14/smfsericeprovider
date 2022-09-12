package com.smf.events.ui.schedulemanagement.model


data class EventDates(
    val success: Boolean,
    val data: Data,
    val result: Result

)

data class Data(
    var serviceDates: List<String>,
    var dayCount: Int,
    var weekCount: Int,
    var monthcount: Int
)

data class Result(var info: String)