package com.smf.events.base

import android.util.Log
import com.amplifyframework.util.Casing.capitalize
import com.google.gson.Gson
import com.smf.events.R
import com.smf.events.SMFApp
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
                errorResponse?.let { ApisResponse.CustomError(capitalize(it)) }
                    ?: ApisResponse.CustomError(SMFApp.appContext.resources.getString(R.string.something_went_wrong))
            }
        } catch (e: HttpException) {
            // Returning HttpException's message(HttpTimeOutException & HandshakeException)
            errorMessageFromApi(e.response()?.errorBody())?.let { ApisResponse.CustomError(it) }
                ?: ApisResponse.CustomError(SMFApp.appContext.resources.getString(R.string.something_went_wrong))
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
                    ApisResponse.InternetError(SMFApp.appContext.resources.getString(R.string.something_went_wrong))
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "safeApiCall: ${e.cause}, ${e.message}, ${e.printStackTrace()}")
            // Returning 'Something went wrong' in case
            ApisResponse.CustomError(SMFApp.appContext.resources.getString(R.string.something_went_wrong))
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