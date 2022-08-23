package com.smf.events.helper

import javax.inject.Inject
import javax.inject.Singleton

// 3103
@Singleton
class ApplicationUtils @Inject constructor() {

    companion object {
        var fromNotification: Boolean = false
        var backArrowNotification: Boolean = false
    }
}