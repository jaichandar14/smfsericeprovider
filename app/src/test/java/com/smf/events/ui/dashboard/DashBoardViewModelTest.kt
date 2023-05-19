package com.smf.events.ui.dashboard

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.smf.events.network.ApiStories
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class DashBoardViewModelTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dashBoardRepository: DashBoardRepository

    @Mock
    lateinit var apiStories: ApiStories

    @Mock
    private lateinit var context: Application
    private lateinit var viewModel: DashBoardViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        dashBoardRepository = DashBoardRepository(apiStories)
        viewModel = Mockito.mock(DashBoardViewModel(dashBoardRepository, context)::class.java)
    }

    @Test
    fun getServiceCount() {
        viewModel.getServiceCount("vignesh", 173)
        Mockito.verify(viewModel, Mockito.times(1))
            .getServiceCount("vignesh", 173)
    }

    @Test
    fun getAllServices() {
        viewModel.getAllServices("vignesh", 173)
        Mockito.verify(viewModel, Mockito.times(1))
            .getAllServices("vignesh", 173)
    }

    @Test
    fun getServicesBranches() {
        viewModel.getServicesBranches("vignesh", 173,1509)
        Mockito.verify(viewModel, Mockito.times(1))
            .getServicesBranches("vignesh", 173,1509)
    }

}