package com.smf.events.helper

data class EvenTypes(
    val success: Boolean,
    val result: Result
)

data class Result(val info: String)
