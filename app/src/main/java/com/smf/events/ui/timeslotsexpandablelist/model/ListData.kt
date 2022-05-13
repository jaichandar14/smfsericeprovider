package com.smf.events.ui.timeslotsexpandablelist.model

data class ListData(val timeSlot: String, val status: List<Status>)

data class Status(
    val ServiceName: String,
    val branchName: String,
    val eventName: String,
    val eventDate: String
)
