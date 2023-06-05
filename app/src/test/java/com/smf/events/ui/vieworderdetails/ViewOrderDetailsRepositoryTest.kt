package com.smf.events.ui.vieworderdetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.actionandstatusdashboard.model.Result
import com.smf.events.ui.vieworderdetails.model.DataValue
import com.smf.events.ui.vieworderdetails.model.OrderDetails
import com.smf.events.ui.vieworderdetails.model.VenueInformationDto
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
class ViewOrderDetailsRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ViewOrderDetailsRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = ViewOrderDetailsRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for viewOrderDetails details, api must return viewOrderDetails details with http code 200`() =
        runBlocking {
            val orderDetails = OrderDetails(
                true,
                DataValue(VenueInformationDto(""), null, ""),
                Result("Success")
            )

            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(orderDetails))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getViewOrderDetails("", 0, 0)) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(orderDetails)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, getViewOrderDetails api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.getViewOrderDetails("", 0, 0)) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

}