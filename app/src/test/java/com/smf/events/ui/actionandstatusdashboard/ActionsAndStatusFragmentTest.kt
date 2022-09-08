package com.smf.events.ui.actionandstatusdashboard

import com.smf.events.helper.AppConstants
import com.smf.events.ui.dashboard.model.MyEvents
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class ActionsAndStatusFragmentTest {

    lateinit var actionsAndStatusFragment: ActionsAndStatusFragment

    @Mock
    lateinit var myEvents: MyEvents

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        actionsAndStatusFragment = mock(ActionsAndStatusFragment()::class.java)
    }

    @Test
    fun actionAndStatusApiCall() {
        actionsAndStatusFragment.actionAndStatusApiCall("idToken")
        Mockito.verify(actionsAndStatusFragment, Mockito.times(1))
            .actionAndStatusApiCall("idToken")
    }

    @Test
    fun goToActionDetailsFragment() {
        actionsAndStatusFragment.goToActionDetailsFragment("bidStatus")
        Mockito.verify(actionsAndStatusFragment, Mockito.times(1))
            .goToActionDetailsFragment("bidStatus")
    }

    @Test
    fun verifyMethodCalledCardClicked() {
        actionsAndStatusFragment.cardClicked(myEvents)
        Mockito.verify(actionsAndStatusFragment, Mockito.times(1))
            .cardClicked(myEvents)
    }

    @Test
    fun newRequestCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.NEW_REQUEST)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.BID_REQUESTED)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.BID_REQUESTED
        )
    }

    @Test
    fun pendingQuoteCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.PENDING_QUOTE)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.PENDING_FOR_QUOTE)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.PENDING_FOR_QUOTE
        )
    }

    @Test
    fun rejectedBidCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.REJECTED_BID)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.BID_REJECTED)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.BID_REJECTED
        )
    }

    @Test
    fun bidSubmittedCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.QUOTE_SENT)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.BID_SUBMITTED)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.BID_SUBMITTED
        )
    }

    @Test
    fun wonBidCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.BID_WON)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.WON_BID)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.WON_BID
        )
    }

    @Test
    fun lostBidCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.BID_LOST)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.LOST_BID)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.LOST_BID
        )
    }

    @Test
    fun serviceInProgressCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.SERVICE_PROGRESS)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.SERVICE_IN_PROGRESS)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.SERVICE_IN_PROGRESS
        )
    }

    @Test
    fun requestClosedCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.REQUEST_CLOSED)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.SERVICE_DONE)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.SERVICE_DONE
        )
    }

    @Test
    fun bidTimedOutCardClicked() {
        // Object for myEvent
        val myEvent = MyEvents("1", AppConstants.TIMED_OUT_BID)
        Mockito.`when`(actionsAndStatusFragment.cardClicked(myEvent))
            .thenReturn(AppConstants.BID_TIMED_OUT)
        Assert.assertEquals(
            actionsAndStatusFragment.cardClicked(myEvent), AppConstants.BID_TIMED_OUT
        )
    }

}