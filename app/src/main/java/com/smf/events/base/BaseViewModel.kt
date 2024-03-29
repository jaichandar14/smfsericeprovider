package com.smf.events.base

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    var showLoading = MutableLiveData<Boolean>(false)
    val networkState = MutableStateFlow<Boolean?>(null)
    val networkStateFlow = networkState as StateFlow<Boolean>
    var toastMessage: String = ""

    init {
        showLoading.value = false
    }

    data class ToastLayoutParam(var msg: String, var duration: Int, var properties: String)

    var toastMessageG = MutableLiveData<ToastLayoutParam>()
    val getToastMessageG: LiveData<ToastLayoutParam> = toastMessageG

    fun setToastMessageG(
        msg: String, duration: Int, properties: String
    ) {
        toastMessageG.value = ToastLayoutParam(msg, duration, properties)
        Log.d("TAG", "setCurrentDate: ${toastMessageG.value}")
    }

    fun showToastMessage(message: String, length: Int, property: String) {
        setToastMessageG(message, length, property)
    }

    fun showProgress() {
        showLoading.value = true
    }

    fun hideProgress() {
        showLoading.value = false
    }

    fun getProgressValue(): Boolean {
        return showLoading.value ?: false
    }
}