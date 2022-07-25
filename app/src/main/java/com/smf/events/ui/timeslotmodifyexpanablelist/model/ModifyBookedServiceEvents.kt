package com.smf.events.ui.timeslotmodifyexpanablelist.model

data class ModifyBookedServiceEvents(
    val data: List<Data>,
    val result: Result,
    val success: Boolean
)

data class Data(
    val serviceSlot: String,
    val bookedEventServiceDtos: List<BookedEventServiceDtoModify>?
)

data class BookedEventServiceDtoModify(
    val branchName: String,
    val eventDate: String,
    val eventName: String,
    val bidStatus: String,
    val preferredSlots: Any
)

data class Result(
    val info: String
)