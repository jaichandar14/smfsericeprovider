package com.smf.events.ui.actionandstatusdashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.ActionAndStatus
import com.smf.events.ui.dashboard.model.DataActionAndStatus
import com.smf.events.ui.dashboard.model.Results
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
internal class ActionsAndStatusRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ActionsAndStatusRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = ActionsAndStatusRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for ActionAndStatus details, api must return ActionAndStatus details with http code 200`() = runBlocking {
        val actionAndStatus = ActionAndStatus(
            true,
            DataActionAndStatus(0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0),
            Results("success")
        )

        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Gson().toJson(actionAndStatus))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getActionAndStatus("", 0, 0, 0)) {
            is ApisResponse.Success -> {
                Truth.assertThat(actualResponse.response).isEqualTo(actionAndStatus)
            }
            else -> {}
        }
    }

    @Test
    fun `for server error, ActionAndStatus details api must return with http error message`() = runBlocking {
        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            .setBody(Gson().toJson(null))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getActionAndStatus("", 0, 0, 0)) {
            is ApisResponse.CustomError -> {
                Truth.assertThat(actualResponse.message)
                    .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
            }
            else -> {}
        }
    }

}