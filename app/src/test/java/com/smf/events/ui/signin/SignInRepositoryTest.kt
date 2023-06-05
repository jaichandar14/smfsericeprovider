package com.smf.events.ui.signin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.signup.model.GetUserData
import com.smf.events.ui.signup.model.GetUserDetails
import com.smf.events.ui.signup.model.GetUserResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.HttpURLConnection

@RunWith(JUnit4::class)
class SignInRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: SignInRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = SignInRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for users details, api must return users details with http code 200`() = runBlocking {
        val getUserDetails = GetUserDetails(
            "true",
            GetUserData(0, 0, "", "", "", "", "", "", "", true),
            GetUserResult("success")
        )

        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Gson().toJson(getUserDetails))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getUserDetails("Vignesh")) {
            is ApisResponse.Success -> {
                assertThat(actualResponse.response).isEqualTo(getUserDetails)
            }
            else -> {}
        }
    }

    @Test
    fun `for server error, GetUserDetails api must return with http error message`() = runBlocking {
        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            .setBody(Gson().toJson(null))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getUserDetails("Vignesh")) {
            is ApisResponse.CustomError -> {
                assertThat(actualResponse.message).isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
            }
            else -> {}
        }
    }

}