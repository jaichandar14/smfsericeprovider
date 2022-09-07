package com.smf.events.ui.timeslotsexpandablelist

import com.smf.events.ui.timeslotsexpandablelist.model.Data
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class MonthExpandableListFragmentTest {

    private lateinit var monthExpandableListFragment: MonthExpandableListFragment

    @Mock
    lateinit var data: Data

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        monthExpandableListFragment = Mockito.mock(MonthExpandableListFragment()::class.java)
    }

    @Test
    fun getBookedEventServices() {
        monthExpandableListFragment.getBookedEventServices(
            "idToken", 173,
            1509, 1540, "09/06/2022",
            "09/06/2022"
        )
        Mockito.verify(monthExpandableListFragment, Mockito.times(1))
            .getBookedEventServices(
                "idToken", 173,
                1509, 1540, "09/06/2022",
                "09/06/2022"
            )
    }

    @Test
    fun getOnlyBookedEvents() {
        monthExpandableListFragment.getOnlyBookedEvents(data)
        Mockito.verify(monthExpandableListFragment, Mockito.times(1))
            .getOnlyBookedEvents(data)
    }

}