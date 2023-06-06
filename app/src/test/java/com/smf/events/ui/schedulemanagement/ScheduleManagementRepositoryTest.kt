package com.smf.events.ui.schedulemanagement

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.schedulemanagement.model.BusinessValidity
import com.smf.events.ui.schedulemanagement.model.Data
import com.smf.events.ui.schedulemanagement.model.Dataes
import com.smf.events.ui.schedulemanagement.model.EventDates
import com.smf.events.ui.timeslotmodifyexpanablelist.model.ModifyBookedServiceEvents
import com.smf.events.ui.timeslotsexpandablelist.model.BookedServiceList
import com.smf.events.ui.timeslotsexpandablelist.model.Result
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
class ScheduleManagementRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ScheduleManagementRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = ScheduleManagementRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for bookedEventServices details, api must return bookedEventServices details with http code 200`() =
        runBlocking {
            val bookedServiceList = BookedServiceList(ArrayList(), Result("Success"), true)
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(bookedServiceList))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getBookedEventServices("", 0, 0, 0, "", "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(bookedServiceList)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, BookedEventServices api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getBookedEventServices("", 0, 0, 0, "", "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

    @Test
    fun `for eventDates details, api must return eventDates details with http code 200`() =
        runBlocking {
            val eventDates = EventDates(
                true, Data(ArrayList(), 0, 0, 0),
                com.smf.events.ui.schedulemanagement.model.Result("Success")
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(eventDates))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getEventDates("", 0, 0, 0, "", "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(eventDates)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getEventDates api must return with http error message`() = runBlocking {
        val expectedResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            .setBody(Gson().toJson(null))
        mockWebServer.enqueue(expectedResponse)

        when (val actualResponse = repository.getEventDates("", 0, 0, 0, "", "")) {
            is ApisResponse.CustomError -> {
                Truth.assertThat(actualResponse.message)
                    .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
            }
            else -> {}
        }
    }

    @Test
    fun `for modifyBookedEventServices details, api must return modifyBookedEventServices details with http code 200`() =
        runBlocking {
            val modifyBookedServiceEvents = ModifyBookedServiceEvents(
                ArrayList(),
                com.smf.events.ui.timeslotmodifyexpanablelist.model.Result("Success"),
                true
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(modifyBookedServiceEvents))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse =
                repository.getModifyBookedEventServices("", 0, 0, 0, true, "", "")) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(modifyBookedServiceEvents)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getModifyBookedEventServices api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse =
                repository.getModifyBookedEventServices("", 0, 0, 0, true, "", "")) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

    @Test
    fun `for businessValiditiy details, api must return businessValiditiy details with http code 200`() =
        runBlocking {
            val businessValidity = BusinessValidity(true, Dataes(""))
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(businessValidity))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getBusinessValiditiy("", 0)) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(businessValidity)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getBusinessValiditiy api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getBusinessValiditiy("", 0)) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

}