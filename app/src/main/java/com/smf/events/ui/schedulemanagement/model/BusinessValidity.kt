package com.smf.events.ui.schedulemanagement.model

data class BusinessValidity(
    var success: Boolean,
    var data: Dataes
)

data class Dataes(var toDate: String)
