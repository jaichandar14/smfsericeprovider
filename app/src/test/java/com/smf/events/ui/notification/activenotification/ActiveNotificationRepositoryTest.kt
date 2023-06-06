package com.smf.events.ui.notification.activenotification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.notification.model.Datas
import com.smf.events.ui.notification.model.MoveOldResponse
import com.smf.events.ui.notification.model.Notification
import com.smf.events.ui.notification.model.Result
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
internal class ActiveNotificationRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ActiveNotificationRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = ActiveNotificationRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for notifications details, api must return notifications details with http code 200`() =
        runBlocking {
            val notification = Notification(ArrayList(), Result(""), true)
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(notification))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getNotifications("", "", true)) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(notification)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, Notifications api must return with http error message`() = runBlocking {
        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            .setBody(Gson().toJson(null))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getNotifications("", "", true)) {
            is ApisResponse.CustomError -> {
                Truth.assertThat(actualResponse.message)
                    .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
            }
            else -> {}
        }
    }

    @Test
    fun `for moveToOldNotification details, api must return moveToOldNotification details with http code 200`() =
        runBlocking {
            val moveOldResponse = MoveOldResponse(Datas(0, 0, "", 0))
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(moveOldResponse))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.moveToOldNotification("", ArrayList())) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(moveOldResponse)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, moveToOldNotification api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.moveToOldNotification("", ArrayList())) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

}