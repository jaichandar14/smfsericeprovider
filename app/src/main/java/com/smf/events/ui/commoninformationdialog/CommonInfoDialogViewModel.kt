package com.smf.events.ui.commoninformationdialog

import android.app.Application
import com.smf.events.base.BaseDialogViewModel
import javax.inject.Inject

class CommonInfoDialogViewModel @Inject constructor(
    private val commonInfoDialogRepository: CommonInfoDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {
}