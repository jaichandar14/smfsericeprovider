package com.smf.events.ui.actionandstatusdashboard

import android.app.Application
import com.smf.events.network.ApiStories
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class ActionsAndStatusViewModelTest {

    private lateinit var actionsAndStatusRepository: ActionsAndStatusRepository

    @Mock
    lateinit var apiStories: ApiStories

    @Mock
    private lateinit var context: Application
    private lateinit var viewModel: ActionsAndStatusViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        actionsAndStatusRepository = ActionsAndStatusRepository(apiStories)
        viewModel =
            Mockito.mock(ActionsAndStatusViewModel(actionsAndStatusRepository, context)::class.java)
    }

    @Test
    fun getActionAndStatus() {
        viewModel.getActionAndStatus(
            "idToken", 173,
            15, 1509
        )
        Mockito.verify(viewModel, Mockito.times(1))
            .getActionAndStatus(
                "idToken", 173,
                15, 1509
            )
    }
}