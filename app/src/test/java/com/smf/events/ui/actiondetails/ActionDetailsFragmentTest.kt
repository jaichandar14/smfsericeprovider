package com.smf.events.ui.actiondetails

import com.smf.events.ui.actiondetails.model.ActionDetails
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class ActionDetailsFragmentTest {

    lateinit var actionDetailsFragment: ActionDetailsFragment

    @Mock
    lateinit var actionDetails: ActionDetails

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        actionDetailsFragment = mock(ActionDetailsFragment()::class.java)
    }

    @Test
    fun showDialog() {
        actionDetailsFragment.showDialog(actionDetails)
        Mockito.verify(actionDetailsFragment, Mockito.times(1))
            .showDialog(actionDetails)
    }

    @Test
    fun postQuoteDetails() {
        actionDetailsFragment.postQuoteDetails(
            1, "Fixed",
            "bidStatus", "100", "latestBidValue",
            "branchName"
        )
        Mockito.verify(actionDetailsFragment, Mockito.times(1))
            .postQuoteDetails(
                1, "Fixed",
                "bidStatus", "100", "latestBidValue",
                "branchName"
            )
    }

    @Test
    fun bidActionsApiCall() {
        actionDetailsFragment.bidActionsApiCall("idToken")
        Mockito.verify(actionDetailsFragment, Mockito.times(1))
            .bidActionsApiCall("idToken")
    }

}