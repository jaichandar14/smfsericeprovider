package com.smf.events.helper

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.smf.events.R


object SnackBar {

    /************************************ ShowSnackbar with message, KeepItDisplayedOnScreen for few seconds */
    fun showSnakbarTypeOne(rootView: View?, mMessage: String?, activity: Activity, duration: Int) {
        if (rootView != null) {
            val snackbar =
                Snackbar.make(rootView, "", duration)
                    .setAction("Action", null)
            // inflate the custom_snackbar_view created previously
            val customSnackView: View =
                activity.layoutInflater.inflate(
                    com.smf.events.R.layout.layout_custome_toast, null
                )
//
//            if (snackbar.isShown){
//                snackbar.dismiss()
//               // setSnackBar(customSnackView,mMessage,snackbar)
//            }else{
            setSnackBar(customSnackView, mMessage, snackbar)
            //  }
        }
    }

    private fun setSnackBar(customSnackView: View, mMessage: String?, snackbar: Snackbar) {
        var textView: TextView = customSnackView.findViewById(R.id.custom_toast_message)

        textView.text = mMessage
        textView.setTextColor(Color.BLACK)
        // set the background of the default snackbar as transparent
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        // now change the layout of the snackbar

        // now change the layout of the snackbar
        val snackbarLayout = snackbar.view as SnackbarLayout

        snackbarLayout.setPadding(10, 0, 0, 0)
        // add the custom snack bar layout to snackbar layout
        snackbarLayout.addView(customSnackView, 0)
        // Position snackbar at top
        // Position snackbar at top


        snackbar.show()
    }

    /************************************ ShowSnackbar with message, KeepItDisplayedOnScreen */
    fun showSnakbarTypeTwo(rootView: View?, mMessage: String?) {
        if (rootView != null) {
            Snackbar.make(rootView, mMessage!!, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null)
                .show()
        }
    }

//    /************************************ ShowSnackbar without message, KeepItDisplayedOnScreen, OnClickOfOk restrat the activity */
//    fun showSnakbarTypeThree(rootView: View?, activity: Activity) {
//        if (rootView != null) {
//            Snackbar
//                .make(rootView, "NoInternetConnectivity", Snackbar.LENGTH_INDEFINITE)
//                .setAction("TryAgain", object : View.OnClickListener() {
//                    fun onClick(view: View?) {
//                        val intent = activity.intent
//                        activity.finish()
//                        activity.startActivity(intent)
//                    }
//                })
//                .setActionTextColor(Color.CYAN)
//                .setCallback(object : Snackbar.Callback() {
//                    override fun onDismissed(snackbar: Snackbar, event: Int) {
//                        super.onDismissed(snackbar, event)
//                    }
//
//                    override fun onShown(snackbar: Snackbar) {
//                        super.onShown(snackbar)
//                    }
//                })
//                .show()
//        }
//    }
//
//    /************************************ ShowSnackbar with message, KeepItDisplayedOnScreen, OnClickOfOk restrat the activity */
//    fun showSnakbarTypeFour(rootView: View?, activity: Activity, mMessage: String?) {
//        Snackbar
//            .make(rootView, mMessage!!, Snackbar.LENGTH_INDEFINITE)
//            .setAction("TryAgain", object : OnClickListener() {
//                fun onClick(view: View?) {
//                    val intent = activity.intent
//                    activity.finish()
//                    activity.startActivity(intent)
//                }
//            })
//            .setActionTextColor(Color.CYAN)
//            .setCallback(object : Snackbar.Callback() {
//                override fun onDismissed(snackbar: Snackbar, event: Int) {
//                    super.onDismissed(snackbar, event)
//                }
//
//                override fun onShown(snackbar: Snackbar) {
//                    super.onShown(snackbar)
//                }
//            })
//            .show()
//    }
}