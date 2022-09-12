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
class DayModifyExpandableListFragmentTest {

    private lateinit var dayModifyExpandableListFragment: DayModifyExpandableListFragment

    @Mock
    lateinit var data: Data

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        dayModifyExpandableListFragment =
            Mockito.mock(DayModifyExpandableListFragment()::class.java)
    }

    @Test
    fun getBookedEventServices() {
        dayModifyExpandableListFragment.getBookedEventServices(
            "idToken", 173,
            1509, 1540, "09/06/2022",
            "09/06/2022", "Day"
        )
        Mockito.verify(dayModifyExpandableListFragment, Mockito.times(1))
            .getBookedEventServices(
                "idToken", 173,
                1509, 1540, "09/06/2022",
                "09/06/2022", "Day"
            )
    }

    @Test
    fun isEmptyAvailableListData() {
        dayModifyExpandableListFragment.isEmptyAvailableListData(data)
        Mockito.verify(dayModifyExpandableListFragment, Mockito.times(1))
            .isEmptyAvailableListData(data)
    }

}