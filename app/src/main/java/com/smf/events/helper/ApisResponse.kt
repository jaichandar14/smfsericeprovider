package com.smf.events.helper

sealed class ApisResponse<out T> {
    data class Success<T>(var response: T) : ApisResponse<T>()
    data class Error(var exception: Exception) : ApisResponse<Nothing>()
    data class CustomError(var message: String) : ApisResponse<Nothing>()
    data class InternetError(var message: String) : ApisResponse<Nothing>()
    object LOADING : ApisResponse<Nothing>()
    object COMPLETED : ApisResponse<Nothing>()
}