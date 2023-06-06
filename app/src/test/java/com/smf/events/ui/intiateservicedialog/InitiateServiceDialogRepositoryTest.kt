package com.smf.events.ui.intiateservicedialog

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.commoninformationdialog.model.Result
import com.smf.events.ui.commoninformationdialog.model.StartService
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
internal class InitiateServiceDialogRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: InitiateServiceDialogRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = InitiateServiceDialogRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for updateServiceStatus details, api must return ServiceStatus details with http code 200`() =
        runBlocking {
            val startService = StartService(
                true,
                ArrayList(),
                Result("", "")
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(startService))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.updateServiceStatus("", 0, 0, 0, "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(startService)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, updateServiceStatus api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.updateServiceStatus("", 0, 0, 0, "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

}