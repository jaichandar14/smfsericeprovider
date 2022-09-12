package com.smf.events.ui.schedulemanagement.calendarfragment

import com.smf.events.helper.CalendarUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class CalendarFragmentTest {

    private lateinit var calendarFragment: CalendarFragment

    @Mock
    lateinit var monthDates: CalendarUtils.MonthDates

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        calendarFragment = Mockito.mock(CalendarFragment()::class.java)
    }

    @Test
    fun getBusinessValidity() {
        calendarFragment.getBusinessValiditiy()
        Mockito.verify(calendarFragment, Mockito.times(1))
            .getBusinessValiditiy()
    }

    @Test
    fun selectedEXPDateObserver() {
        calendarFragment.selectedEXPDateObserver()
        Mockito.verify(calendarFragment, Mockito.times(1))
            .selectedEXPDateObserver()
    }

    @Test
    fun settingMonthDate() {
        calendarFragment.settingMonthDate()
        Mockito.verify(calendarFragment, Mockito.times(1))
            .settingMonthDate()
    }

    @Test
    fun setCurrentMonth() {
        calendarFragment.setCurrentMonth(monthDates)
        Mockito.verify(calendarFragment, Mockito.times(1))
            .setCurrentMonth(monthDates)
    }

    @Test
    fun settingWeekDate() {
        calendarFragment.settingWeekDate()
        Mockito.verify(calendarFragment, Mockito.times(1))
            .settingWeekDate()
    }

    @Test
    fun getAllServices() {
        calendarFragment.getAllServices()
        Mockito.verify(calendarFragment, Mockito.times(1))
            .getAllServices()
    }

    @Test
    fun getBranches() {
        calendarFragment.getBranches("idToken", 1509)
        Mockito.verify(calendarFragment, Mockito.times(1))
            .getBranches("idToken", 1509)
    }

    @Test
    fun eventDateAndCounts() {
        calendarFragment.eventDateAndCounts(
            1509, 2,
            "idToken", true
        )
        Mockito.verify(calendarFragment, Mockito.times(1))
            .eventDateAndCounts(1509, 2, "idToken", true)
    }

}