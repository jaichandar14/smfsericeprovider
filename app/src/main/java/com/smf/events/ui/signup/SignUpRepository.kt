package com.smf.events.ui.signup

import com.google.gson.Gson
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.signup.model.ErrorResponse
import com.smf.events.ui.signup.model.UserDetails
import com.smf.events.ui.signup.model.UserDetailsResponse
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SignUpRepository @Inject constructor(var apiStories: ApiStories) {

    suspend fun setUserDetails(userDetails: UserDetails): ApisResponse<UserDetailsResponse> {

        return try {
            val callApi = apiStories.addUserDetails(userDetails)
            ApisResponse.Success(callApi)
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