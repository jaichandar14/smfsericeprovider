package com.smf.events.helper

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.smf.events.R

object Analytics {
var firebaseAnalytics=Firebase.analytics
fun logEvent(msg:Bundle,eventType:String,key:String){
    firebaseAnalytics.
    logEvent(eventType){
        param(key,msg)
    }
}
    fun setUserId(id:String){
        firebaseAnalytics.setUserId(id)
    }
}