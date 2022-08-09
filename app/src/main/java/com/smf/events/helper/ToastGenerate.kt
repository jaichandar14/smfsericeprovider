package com.smf.events.helper

import android.widget.Toast

import android.view.Gravity

import android.content.Context
import android.graphics.Color

import android.widget.TextView

import android.widget.LinearLayout

import android.view.LayoutInflater
import android.view.View
import com.smf.events.R


class ToastGenerate(var context: Context) {
    //pass message and message type to this method
    fun createToastMessage(message: String, type: Int) {

//inflate the custom layout
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val toastLayout =
            layoutInflater.inflate(R.layout.layout_custome_toast, null) as LinearLayout
        val toastShowMessage = toastLayout.findViewById<TextView>(R.id.custom_toast_message) as TextView
        when (type) {
            0 ->                     //if the message type is 0 fail toaster method will call
                createFailToast(toastLayout, toastShowMessage, message)
            1 ->                     //if the message type is 1 success toaster method will call
                createSuccessToast(toastLayout, toastShowMessage, message)
            2 -> createWarningToast(toastLayout, toastShowMessage, message)
            else -> createFailToast(toastLayout, toastShowMessage, message)
        }
    }

    //Failure toast message method
    private fun createFailToast(
        toastLayout: LinearLayout,
        toastMessage: TextView,
        message: String,
    ) {
        toastLayout.setBackgroundColor(Color.RED)
        toastMessage.text = message
        toastMessage.setTextColor(context.getResources().getColor(R.color.white))
        showToast(context, toastLayout)
    }

    //warning toast message method
    private fun createWarningToast(
        toastLayout: LinearLayout,
        toastMessage: TextView,
        message: String,
    ) {
        toastLayout.setBackgroundColor(Color.YELLOW)
        toastMessage.text = message
        toastMessage.setTextColor(context.getResources().getColor(R.color.white))
        showToast(context, toastLayout)
    }

    //success toast message method
    private fun createSuccessToast(
        toastLayout: LinearLayout,
        toastMessage: TextView,
        message: String,
    ) {
        toastLayout.setBackgroundColor(Color.GREEN)
        toastMessage.text = message
        toastMessage.setTextColor(Color.WHITE)
        showToast(context, toastLayout)
    }

    private fun showToast(context: Context,toastLayout: LinearLayout) {
        val toast = Toast(context)
        toast.setGravity(Gravity.BOTTOM, 0, 0) // show message in the Bottom of the device
        toast.duration = Toast.LENGTH_SHORT
        toast.view=toastLayout
        toast.show()
    }

    companion object {
        private var ourInstance: ToastGenerate? = null
        fun getInstance(context: Context): ToastGenerate? {
            if (ourInstance == null) ourInstance = ToastGenerate(context)
            return ourInstance
        }
    }

    init {
        context = context
    }
}