package com.smf.events.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel

open class BaseDialogViewModel(application: Application): AndroidViewModel(application) {

    var toastMessage: String = ""}