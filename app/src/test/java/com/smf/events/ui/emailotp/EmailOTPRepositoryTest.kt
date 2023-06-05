package com.smf.events.ui.emailotp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.emailotp.model.Datas
import com.smf.events.ui.emailotp.model.GetLoginInfo
import com.smf.events.ui.emailotp.model.OTPValidation
import com.smf.events.ui.emailotp.model.Result
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
internal class EmailOTPRepositoryTest {
    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: EmailOTPRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = EmailOTPRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for LoginInfo details, api must return LoginInfo details with http code 200`() =
        runBlocking {
            val getLoginInfo = GetLoginInfo(
                Datas("", "", true, "", "", "", 0, 0, 0, "", ""),
                true
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(getLoginInfo))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getLoginInfo("")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(getLoginInfo)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getLoginInfo api must return with http error message`() = runBlocking {
        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            .setBody(Gson().toJson(null))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getLoginInfo("")) {
            is ApisResponse.CustomError -> {
                Truth.assertThat(actualResponse.message)
                    .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
            }
            else -> {}
        }
    }

    @Test
    fun `for otpValidation details, api must return otpValidation details with http code 200`() =
        runBlocking {
            val oTPValidation = OTPValidation(true, 0, Result("Success"), "")
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(oTPValidation))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getOtpValidation(true, "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(oTPValidation)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getOtpValidation api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getOtpValidation(true, "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

}