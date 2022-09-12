package com.smf.events.ui.emailotp.model

data class OTPValidation(
    var success: Boolean,
    var data: Int,
    var result: Result,
    var errorMessage: String
)


data class Result(var info: String)