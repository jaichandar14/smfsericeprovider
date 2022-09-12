package com.smf.events.ui.timeslotsexpandablelist

import android.app.Application
import com.smf.events.base.BaseViewModel
import javax.inject.Inject

class TimeSlotsExpandableListViewModel @Inject constructor(
    private val timeSlotsExpandableListRepository: TimeSlotsExpandableListRepository,
    application: Application
) : BaseViewModel(application)