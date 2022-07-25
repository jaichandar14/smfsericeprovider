package com.smf.events.ui.timeslotsexpandablelist.model

import com.smf.events.ui.timeslotmodifyexpanablelist.model.BookedEventServiceDtoModify

data class ListData(val timeSlot: String, val status: List<BookedEventServiceDto>)
data class ListDataModify(val timeSlot: String, val status: List<BookedEventServiceDtoModify>)
