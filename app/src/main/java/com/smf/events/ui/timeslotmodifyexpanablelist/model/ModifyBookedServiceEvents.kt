package com.smf.events.ui.timeslotmodifyexpanablelist.model

import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventServiceDto

data class ModifyBookedServiceEvents(
    val data: List<Data>,
    val result: Result,
    val success: Boolean
)

data class Data(
    val serviceSlot: String,
    val bookedEventServiceDtos: List<BookedEventServiceDto>?
)

data class Result(
    val info: String
)