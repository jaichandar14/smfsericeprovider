package com.smf.events.ui.actiondetails

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.smf.events.network.ApiStories
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class ActionDetailsViewModelTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var actionDetailsRepository: ActionDetailsRepository

    @Mock
    lateinit var apiStories: ApiStories

    @Mock
    private lateinit var context: Application
    private lateinit var viewModel: ActionDetailsViewModel

    @Mock
    lateinit var biddingQuotDto: BiddingQuotDto

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        actionDetailsRepository = ActionDetailsRepository(apiStories)
        viewModel =
            Mockito.mock(ActionDetailsViewModel(actionDetailsRepository, context)::class.java)
    }

    @Test
    fun postQuoteDetails() {
        viewModel.postQuoteDetails("idToken", 2, biddingQuotDto)
        Mockito.verify(viewModel, Mockito.times(1))
            .postQuoteDetails("idToken", 2, biddingQuotDto)
    }

    @Test
    fun getBidActions() {
        viewModel.getBidActions(
            "idToken", 173, 1509,
            1509, "bidStatus"
        )
        Mockito.verify(viewModel, Mockito.times(1))
            .getBidActions(
                "idToken", 173, 1509,
                1509, "bidStatus"
            )
    }
}