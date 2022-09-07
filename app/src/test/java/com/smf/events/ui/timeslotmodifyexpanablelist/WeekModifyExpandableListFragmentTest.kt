package com.smf.events.ui.timeslotmodifyexpanablelist

import com.smf.events.ui.timeslotmodifyexpanablelist.model.Data
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class WeekModifyExpandableListFragmentTest {

    private lateinit var monthModifyExpandableListFragment: MonthModifyExpandableListFragment

    @Mock
    lateinit var data: Data

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        monthModifyExpandableListFragment =
            Mockito.mock(MonthModifyExpandableListFragment()::class.java)
    }

    @Test
    fun monthValidation() {
        monthModifyExpandableListFragment.monthValidation()
        Mockito.verify(monthModifyExpandableListFragment, Mockito.times(1))
            .monthValidation()
    }

    @Test
    fun getBookedEventServices() {
        monthModifyExpandableListFragment.getBookedEventServices(
            "idToken", 173,
            1509, 1540, "09/06/2022",
            "09/06/2022", "Week"
        )
        Mockito.verify(monthModifyExpandableListFragment, Mockito.times(1))
            .getBookedEventServices(
                "idToken", 173,
                1509, 1540, "09/06/2022",
                "09/06/2022", "Week"
            )
    }

    @Test
    fun updateUpcomingEvents() {
        monthModifyExpandableListFragment.updateUpcomingEvents(data)
        Mockito.verify(monthModifyExpandableListFragment, Mockito.times(1))
            .updateUpcomingEvents(data)
    }

    @Test
    fun isEmptyAvailableListData() {
        monthModifyExpandableListFragment.isEmptyAvailableListData(data)
        Mockito.verify(monthModifyExpandableListFragment, Mockito.times(1))
            .isEmptyAvailableListData(data)
    }
}