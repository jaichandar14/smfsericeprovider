package com.smf.events.ui.signin

import com.google.gson.Gson
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.signup.model.ErrorResponse
import com.smf.events.ui.signup.model.GetUserDetails
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SignInRepository @Inject constructor(var apiStories: ApiStories) {

    suspend fun getUserDetails(loginName: String): ApisResponse<GetUserDetails> {

        return try {
            val getResponse = apiStories.getUserDetails(loginName)
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