package com.smf.events.ui.emailotp

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.emailotp.model.GetLoginInfo
import com.smf.events.ui.emailotp.model.OTPValidation
import javax.inject.Inject

class EmailOTPRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {


    suspend fun getLoginInfo(idToken: String): ApisResponse<GetLoginInfo> {
        return safeApiCall { apiStories.getLoginInfo(idToken) }
    }

    suspend fun getOtpValidation(isValid: Boolean, username: String): ApisResponse<OTPValidation> {
        return safeApiCall { apiStories.setOTPValidation(isValid, username) }
    }

}