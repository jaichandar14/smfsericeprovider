package com.smf.events.ui.emailotp

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.emailotp.model.GetLoginInfo
import retrofit2.HttpException
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


}