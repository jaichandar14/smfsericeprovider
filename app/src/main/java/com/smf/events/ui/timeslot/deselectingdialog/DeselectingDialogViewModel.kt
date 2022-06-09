package com.smf.events.ui.timeslot.deselectingdialog

import android.annotation.SuppressLint
import android.app.Application
import android.view.View
import com.smf.events.R
import com.smf.events.base.BaseDialogViewModel
import com.smf.events.databinding.FragmentDeseletingDialogBinding
import com.smf.events.ui.timeslot.deselectingdialog.adaptor.DeselectedDialogAdaptor
import com.smf.events.ui.timeslot.deselectingdialog.model.ListData
import javax.inject.Inject

class DeselectingDialogViewModel @Inject constructor(
    application: Application,
) : BaseDialogViewModel(application) {

}