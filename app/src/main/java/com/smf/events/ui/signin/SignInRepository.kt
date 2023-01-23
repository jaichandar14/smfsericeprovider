package com.smf.events.ui.signin

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.signup.model.GetUserDetails
import javax.inject.Inject

class SignInRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    suspend fun getUserDetails(loginName: String): ApisResponse<GetUserDetails> {
        return safeApiCall { apiStories.getUserDetails(loginName) }
    }

}