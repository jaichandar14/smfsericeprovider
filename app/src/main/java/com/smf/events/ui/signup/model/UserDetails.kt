package com.smf.events.ui.signup.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDetails(
    var role: String,
    var firstName: String,
    var lastName: String,
    var email: String,
    var mobileNumber: String,
    var userName: String
) : Parcelable

//Response
@Parcelize
data class UserDetailsResponse(var success: Boolean, var data: Data, var result: Results) :
    Parcelable

@Parcelize
data class Data(var status: String, var statusCode: Int, var profileId: Int, var key: Int) :
    Parcelable

@Parcelize
data class Results(var info: String) : Parcelable


@Parcelize
data class ErrorResponse(
    var id: Int,
    var errorMessage: String,
    var errorCode: String,
    var timeStamp: String,
    var exceptionMessage: String
) : Parcelable

