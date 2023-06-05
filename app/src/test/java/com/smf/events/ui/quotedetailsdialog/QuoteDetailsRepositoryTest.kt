package com.smf.events.ui.quotedetailsdialog

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.smf.events.RetrofitHelper
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.network.ApiStories
import com.smf.events.ui.actionandstatusdashboard.model.Data
import com.smf.events.ui.actionandstatusdashboard.model.NewRequestList
import com.smf.events.ui.actionandstatusdashboard.model.Result
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
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
class QuoteDetailsRepositoryTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: QuoteDetailsRepository
    private lateinit var testApis: ApiStories
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        testApis = RetrofitHelper.testApiInstance(mockWebServer.url("/").toString())
            .create(ApiStories::class.java)
        repository = QuoteDetailsRepository(testApis)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `for postQuoteDetails, api must return postQuoteDetails with http code 200`() =
        runBlocking {
            val newRequestList = NewRequestList(
                Data(0.0, 0.0, 0.0, null, 0.0),
                Result("success"),
                true
            )
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Gson().toJson(newRequestList))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.postQuoteDetails(
                "",
                0,
                BiddingQuotDto(bidRequestId = 0, latestBidValue = 0)
            )) {
                is ApisResponse.Success -> {
                    Truth.assertThat(actualResponse.response).isEqualTo(newRequestList)
                }
                else -> {}
            }
        }

    @Test
    fun `for server error, postQuoteDetails api must return with http error message`() =
        runBlocking {
            val expectedResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody(Gson().toJson(null))
            mockWebServer.enqueue(expectedResponse)

            when (val actualResponse = repository.postQuoteDetails(
                "",
                0,
                BiddingQuotDto(bidRequestId = 0, latestBidValue = 0)
            )) {
                is ApisResponse.CustomError -> {
                    Truth.assertThat(actualResponse.message)
                        .isEqualTo(AppConstants.SOMETHING_WENT_WRONG)
                }
                else -> {}
            }
        }

}