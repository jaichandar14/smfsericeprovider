package com.smf.events.ui.notification.oldnotification

import android.app.Application
import com.smf.events.base.BaseViewModel
import javax.inject.Inject

class OldNotificationViewModel @Inject constructor(
    private val oldNotificationRepository: OldNotificationRepository,
    application: Application
) : BaseViewModel(application)  {
}