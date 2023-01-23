package com.smf.events.base

import android.util.Log
import com.google.gson.Gson
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.ui.signup.model.ErrorResponse
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseRepo {

    val TAG = this::class.java.name

    // we'll use this function in all
    // repos to handle api errors.
    suspend fun <T> safeApiCall(apiToBeCalled: suspend () -> Response<T>): ApisResponse<T> {

        return try {
            // Here we are calling api lambda
            // function that will return response
            // wrapped in Retrofit's Response class
            val response: Response<T> = apiToBeCalled()

            if (response.isSuccessful) {
                ApisResponse.Success(response.body()!!)
            } else {
                // parsing api own custom json error
                val errorResponse: String? = errorMessageFromApi(response.errorBody())
                // Returning api own failure message
                errorResponse?.let { ApisResponse.CustomError(it) }
                    ?: ApisResponse.CustomError("Something went wrong")
            }
        } catch (e: HttpException) {
            // Returning HttpException's message
            errorMessageFromApi(e.response()?.errorBody())?.let { ApisResponse.CustomError(it) }
                ?: ApisResponse.CustomError("Something went wrong")
        } catch (e: IOException) {
            when (e) {
                is ConnectException -> {
                    Log.d(TAG, "safeApiCall: ConnectException ")
                    ApisResponse.InternetError(AppConstants.SHOW_INTERNET_DIALOG)
                }
                is SocketException -> {
                    Log.d(TAG, "safeApiCall: SocketException")
                    // Returning no internet message
                    ApisResponse.InternetError("SocketException")
                }
                is SocketTimeoutException -> {
                    Log.d(TAG, "safeApiCall: SocketTimeoutException")
                    // Returning no internet message
                    ApisResponse.InternetError("SocketTimeoutException")
                }
                is UnknownHostException -> {
                    Log.d(TAG, "safeApiCall: UnknownHostException")
                    ApisResponse.InternetError(AppConstants.SHOW_INTERNET_DIALOG)
                }
                else -> {
                    Log.d(TAG, "safeApiCall: SocketException")
                    ApisResponse.InternetError("Something went wrong")
                }
            }
        } catch (e: Exception) {
            // Returning 'Something went wrong' in case
            ApisResponse.CustomError("Something went wrong")
        }
    }

    // Getting error messages from api's
    private fun errorMessageFromApi(errorBody: ResponseBody?): String? {
        var errorMessage: String? = null
        try {
            val adapter = Gson().getAdapter(ErrorResponse::class.java)
            val errorParser = adapter.fromJson(errorBody?.string())
            Log.d(TAG, "errorMessageApi: ${errorParser.errorMessage} ${errorParser.message}")
            errorMessage = if (errorParser.errorMessage != null) {
                errorParser.errorMessage
            } else {
                errorParser.message
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return errorMessage
    }

}