package com.smf.events.listeners

import androidx.fragment.app.DialogFragment

interface DialogTwoButtonListener : DialogOneButtonListener {
    fun onNegativeClick(dialogFragment : DialogFragment)
}