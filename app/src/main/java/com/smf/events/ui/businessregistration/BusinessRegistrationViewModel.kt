package com.smf.events.ui.businessregistration

import android.app.Application
import com.smf.events.base.BaseViewModel
import com.smf.events.ui.dashboard.DashBoardRepository
import javax.inject.Inject

class BusinessRegistrationViewModel @Inject constructor(
    private val businessRegistrationRepository: BusinessRegistrationRepository,
    application: Application
) : BaseViewModel(application) {
}