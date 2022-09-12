package com.smf.events.ui.notification.activenotification

import android.app.Application
import com.smf.events.base.BaseViewModel
import javax.inject.Inject

class ActiveNotificationViewModel @Inject constructor(
    private val activeNotificationRepository: ActiveNotificationRepository,
    application: Application
) : BaseViewModel(application)