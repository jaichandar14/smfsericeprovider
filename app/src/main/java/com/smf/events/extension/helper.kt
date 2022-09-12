package com.smf.events.extension

import java.util.regex.Pattern

fun String.isEmailValid(email: String): Boolean {
    val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(email)
    if (matcher.matches()) {
        return matcher.matches()
    } else {
        //txtFields.error = alert
        // requestFocus(txtFields, window)
        return matcher.matches()
    }
}