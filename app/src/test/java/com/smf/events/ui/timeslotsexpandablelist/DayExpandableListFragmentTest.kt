package com.smf.events.ui.timeslotsexpandablelist

import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslotsexpandablelist.model.Data
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class DayExpandableListFragmentTest {

    private lateinit var dayExpandableListFragment: DayExpandableListFragment

    @Mock
    lateinit var selectedDate: ScheduleManagementViewModel.SelectedDate
    @Mock
    lateinit var data: Data

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        dayExpandableListFragment = Mockito.mock(DayExpandableListFragment()::class.java)
    }

    @Test
    fun setListOfDatesArrayList() {
        dayExpandableListFragment.setListOfDatesArrayList(selectedDate)
        Mockito.verify(dayExpandableListFragment, Mockito.times(1))
            .setListOfDatesArrayList(selectedDate)
    }

    @Test
    fun getBookedEventServices() {
        dayExpandableListFragment.getBookedEventServices(
            "idToken", 173,
            1509, 1540, "09/06/2022",
            "09/06/2022", "Day"
        )
        Mockito.verify(dayExpandableListFragment, Mockito.times(1))
            .getBookedEventServices(
                "idToken", 173,
                1509, 1540, "09/06/2022",
                "09/06/2022", "Day"
            )
    }

    @Test
    fun getOnlyBookedEvents() {
        dayExpandableListFragment.getOnlyBookedEvents(data)
        Mockito.verify(dayExpandableListFragment, Mockito.times(1))
            .getOnlyBookedEvents(data)
    }

}