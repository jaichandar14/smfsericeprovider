package com.smf.events.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.*
import com.smf.events.ui.notification.model.DataCount
import com.smf.events.ui.notification.model.NotificationCount
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
internal class DashBoardRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: DashBoardRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = DashBoardRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for getServiceCount details, api must return getServiceCount details with http code 200`() =
        runBlocking {
            val serviceCount = ServiceCount(
                true,
                Datas(0, 0, 0, 0, 0, 0),
                Result("success")
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(serviceCount))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getServiceCount("", 0)) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(serviceCount)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getServiceCount api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getServiceCount("", 0)) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

    @Test
    fun `for getAllServices details, api must return getAllServices details with http code 200`() =
        runBlocking {
            val allServices = AllServices(
                ArrayList(),
                Result(""),
                true
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(allServices))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getAllServices("", 0)) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(allServices)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getAllServices api must return with http error message`() = runBlocking {
        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            .setBody(Gson().toJson(null))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getAllServices("", 0)) {
            is ApisResponse.CustomError -> {
                Truth.assertThat(actualResponse.message)
                    .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
            }
            else -> {}
        }
    }

    @Test
    fun `for getServicesBranches details, api must return getServicesBranches details with http code 200`() =
        runBlocking {
            val branches = Branches(
                true,
                ArrayList(),
                Result("Success")
            )
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(branches))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getServicesBranches("", 0, 0)) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(branches)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getServicesBranches api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getServicesBranches("", 0, 0)) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

    @Test
    fun `for get notificationCount, api must return notificationCount details with http code 200`() =
        runBlocking {
            val notificationCount = NotificationCount(DataCount(0, 0))
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(notificationCount))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getNotificationCount("", "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(notificationCount)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getNotificationCount api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getNotificationCount("", "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }


}