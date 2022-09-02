package com.smf.events.ui.dashboard

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
class DashBoardFragmentTest {

    private lateinit var dashBoardFragment: DashBoardFragment

    @Before
    fun setUp() {
        dashBoardFragment = mock(DashBoardFragment()::class.java)
    }

    @Test
    fun sideNavBarInitialization() {
        dashBoardFragment.sideNavBarInitialization()
        Mockito.verify(dashBoardFragment, Mockito.times(1))
            .sideNavBarInitialization()
    }

    @Test
    fun actionAndStatusFragmentMethod() {
        dashBoardFragment.actionAndStatusFragmentMethod(173, 1056)
        Mockito.verify(dashBoardFragment, Mockito.times(1))
            .actionAndStatusFragmentMethod(173, 1056)
    }

    @Test
    fun getAllServiceAndCounts() {
        dashBoardFragment.getAllServiceAndCounts("idToken")
        Mockito.verify(dashBoardFragment, Mockito.times(1))
            .getAllServiceAndCounts("idToken")
    }

    @Test
    fun getAllServices() {
        dashBoardFragment.getAllServices("idToken")
        Mockito.verify(dashBoardFragment, Mockito.times(1))
            .getAllServices("idToken")
    }

    @Test
    fun getBranches() {
        dashBoardFragment.getBranches("idToken", 1056)
        Mockito.verify(dashBoardFragment, Mockito.times(1))
            .getBranches("idToken", 1056)
    }

    @Test
    fun itemClick() {
        dashBoardFragment.itemClick(1)
        Mockito.verify(dashBoardFragment, Mockito.times(1))
            .itemClick(1)
    }

    @Test
    fun branchItemClick() {
        dashBoardFragment.branchItemClick(1056,"Branch",1)
        Mockito.verify(dashBoardFragment, Mockito.times(1))
            .branchItemClick(1056,"Branch",1)
    }
}