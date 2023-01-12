package com.smf.events.rxbus

import android.app.Dialog
import com.smf.events.ui.dashboard.model.ActionAndStatusCount
import com.smf.events.ui.dashboard.model.ServiceAndCategoryId

class RxEvent {
    data class QuoteBrief(var bidReqId: Int, var status: Boolean)
    data class QuoteBrief1(var bidReqId: Int, var status: Boolean)
    data class ModifyDialog(val status: String)
    data class ChangingNav(val str: Int)
    data class DenyStorage(val s: Boolean)
    data class ChangingNavDialog(val str: Dialog?)
    data class IsValid(val str: Boolean)
    data class InternetStatus(val status: Boolean)
    data class ClearAllNotification(val onClick: Boolean)
    data class UpdateNotificationCount(val tag: String)
}