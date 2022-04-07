package com.smf.events.ui.actionandstatusdashboard.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceProviderBidRequestList(
    val bidRequestId: Int,
    val branchName: String,
    val eventName: String,
): Parcelable
