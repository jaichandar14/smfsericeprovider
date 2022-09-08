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
class WeekExpandableListFragmentTest {

    private lateinit var weekExpandableListFragment: WeekExpandableListFragment

    @Mock
    lateinit var data: Data

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        weekExpandableListFragment = Mockito.mock(WeekExpandableListFragment()::class.java)
    }

    @Test
    fun setListOfDatesArrayList() {
        weekExpandableListFragment.getBookedEventServices(
            "idToken", 173,
            1509, 1540, "09/06/2022",
            "09/06/2022", "Day"
        )
        Mockito.verify(weekExpandableListFragment, Mockito.times(1))
            .getBookedEventServices(
                "idToken", 173,
                1509, 1540, "09/06/2022",
                "09/06/2022", "Day"
            )
    }

    @Test
    fun getOnlyBookedEvents() {
        weekExpandableListFragment.getOnlyBookedEvents(data)
        Mockito.verify(weekExpandableListFragment, Mockito.times(1))
            .getOnlyBookedEvents(data)
    }

    @Test
    fun initializeExpandableListSetUp() {
        weekExpandableListFragment.initializeExpandableListSetUp("Caller")
        Mockito.verify(weekExpandableListFragment, Mockito.times(1))
            .initializeExpandableListSetUp("Caller")
    }
}