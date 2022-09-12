package com.smf.events.base

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class BaseDialogViewModel(application: Application) : AndroidViewModel(application) {

    var toastMessage: String = ""

    data class ToastLayoutParam(var msg: String, var duration: Int, var properties: String)

    private var toastMessageG = MutableLiveData<ToastLayoutParam>()
    val getToastMessageG: LiveData<ToastLayoutParam> = toastMessageG
    fun setToastMessageG(
        msg: String, duration: Int, properties: String
    ) {
        toastMessageG.value = ToastLayoutParam(msg, duration, properties)
        Log.d("TAG", "setCurrentDate: ${toastMessageG.value}")
    }
}