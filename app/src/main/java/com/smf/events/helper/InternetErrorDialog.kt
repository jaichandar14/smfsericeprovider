package com.smf.events.helper

import android.R
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment

class InternetErrorDialog : DialogFragment() {

    var noInternetDialog: Dialog? = null

    companion object {
        const val TAG = "InternetErrorDialog"
        fun newInstance(): InternetErrorDialog {
            return InternetErrorDialog()
        }
    }

    fun checkInternetAvailable(context: Context): Boolean {
        return if (!SharedPreference.isInternetConnected) {
            showFullScreenDialog(context)
            false
        } else {
            true
        }
    }

    private fun showFullScreenDialog(context: Context) {
        noInternetDialog = Dialog(context, R.style.Theme)
        noInternetDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        noInternetDialog?.setContentView(com.smf.events.R.layout.dialog_no_internet)
        noInternetDialog?.setCancelable(false)
        val window = noInternetDialog?.window
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val retryBtn: AppCompatButton? =
            noInternetDialog?.findViewById(com.smf.events.R.id.btn_retry)
        retryBtn?.setOnClickListener {
            if (SharedPreference.isInternetConnected) {
                noInternetDialog?.dismiss()
            } else {
                Toast.makeText(
                    context,
                    "Internet Connection is not available. Please check your network connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        noInternetDialog?.show()
    }

    fun dismissDialog() {
        if (noInternetDialog?.isShowing == true) {
            noInternetDialog?.dismiss()
        }
    }
}