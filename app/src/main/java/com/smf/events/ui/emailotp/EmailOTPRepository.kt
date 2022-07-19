package com.smf.events.ui.emailotp

import com.google.gson.Gson
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.emailotp.model.GetLoginInfo
import com.smf.events.ui.emailotp.model.OTPValidation
import com.smf.events.ui.signup.model.ErrorResponse
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class EmailOTPRepository @Inject constructor(var apiStories: ApiStories) {


    suspend fun getLoginInfo(idToken: String): ApisResponse<GetLoginInfo> {

        return try {
            val getResponse = apiStories.getLoginInfo(idToken)
            ApisResponse.Success(getResponse)

        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
    suspend fun getOtpValidation(isValid:Boolean,username:String): ApisResponse<OTPValidation> {

        return try {
            val getResponse = apiStories.setOTPValidation(isValid,username)
            ApisResponse.Success(getResponse)

        } catch (e: HttpException) {
            ApisResponse.Error(e)
            val errorMessage = errorMessagefromapi(e)
            ApisResponse.CustomError(errorMessage!!)
        }
    }

    private fun errorMessagefromapi(httpException: HttpException): String? {
        var errorMessage: String? = null
        val error = httpException.response()?.errorBody()

        try {

            val adapter = Gson().getAdapter(ErrorResponse::class.java)
            val errorParser = adapter.fromJson(error?.string())
            errorMessage = errorParser.errorMessage
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            return errorMessage
        }
    }

}