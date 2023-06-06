package com.smf.events.ui.timeslot.deselectingdialog

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.timeslot.deselectingdialog.model.Data
import com.smf.events.ui.timeslot.deselectingdialog.model.ModifyDaySlotResponse
import com.smf.events.ui.timeslot.deselectingdialog.model.Result
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
class DeselectingDialogRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: DeselectingDialogRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = DeselectingDialogRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for modifyDaySlot details, api must return modifyDaySlot details with http code 200`() =
        runBlocking {
            val modifyDaySlotResponse = ModifyDaySlotResponse(
                Data("", 0, "", 0),
                Result("Success"),
                true
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(modifyDaySlotResponse))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getModifyDaySlot("", 0, "", true, "", 0, "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(modifyDaySlotResponse)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getModifyDaySlot api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getModifyDaySlot("", 0, "", true, "", 0, "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

    @Test
    fun `for modifyWeekSlot details, api must return modifyWeekSlot details with http code 200`() =
        runBlocking {
            val modifyDaySlotResponse = ModifyDaySlotResponse(
                Data("", 0, "", 0),
                Result("Success"),
                true
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(modifyDaySlotResponse))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getModifyWeekSlot("", 0, "", true, "", 0, "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(modifyDaySlotResponse)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getModifyWeekSlot api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getModifyWeekSlot("", 0, "", true, "", 0, "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

    @Test
    fun `for modifyMonthSlot details, api must return modifyMonthSlot details with http code 200`() =
        runBlocking {
            val modifyDaySlotResponse = ModifyDaySlotResponse(
                Data("", 0, "", 0),
                Result("Success"),
                true
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(modifyDaySlotResponse))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getModifyMonthSlot("", 0, "", true, "", 0, "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(modifyDaySlotResponse)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getModifyMonthSlot api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getModifyMonthSlot("", 0, "", true, "", 0, "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

}