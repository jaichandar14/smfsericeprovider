package com.smf.events.ui.actionandstatusdashboard

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
class ActionsAndStatusFragmentTest {

    lateinit var actionsAndStatusFragment: ActionsAndStatusFragment

    @Before
    fun setUp() {
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

}