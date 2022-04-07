package com.smf.events.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

abstract class BaseViewModel(application:Application):AndroidViewModel(application) {

    var toastMessage: String = ""
}