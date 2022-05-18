package com.smf.events.ui.timeslotsexpandablelist.model

data class BookedServiceList(
    val data: List<Data>,
    val result: Result,
    val success: Boolean
)


data class Data(
    val bookedEventServiceDtos: List<BookedEventServiceDto>,
    val serviceSlot: String
)

data class BookedEventServiceDto(
    val branchName: String,
    val eventDate: String,
    val eventName: String,
    val preferredSlots: Any
)

data class Result(
    val info: String
)



