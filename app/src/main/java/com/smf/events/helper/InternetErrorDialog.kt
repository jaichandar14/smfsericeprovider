package com.smf.events.helper

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.smf.events.R
import com.smf.events.databinding.DialogNoInternetBinding
import com.smf.events.listeners.DialogTwoButtonListener

class InternetErrorDialog : DialogFragment() {

    val TAG = this::class.java.name
    private lateinit var twoButtonListener: DialogTwoButtonListener
    private lateinit var dataBinding: DialogNoInternetBinding

    companion object {
        fun newInstance(twoButtonListener: DialogTwoButtonListener): InternetErrorDialog {
            val internetDialog = InternetErrorDialog()
            internetDialog.twoButtonListener = twoButtonListener
            return internetDialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_no_internet, container, false)
        return dataBinding.root
    }

    override fun getTheme(): Int {
        return R.style.InternetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setText()
        setupClickListeners()
        Log.d(TAG, "onViewCreated: args ${arguments?.getString(AppConstants.MESSAGE)}")
    }

    private fun setupClickListeners() {
        dataBinding.btnRetry.setOnClickListener {
            twoButtonListener.onPositiveClick(this)
        }
    }

    private fun setText() {
        when (arguments?.getString(AppConstants.MESSAGE)) {
            AppConstants.SHOW_INTERNET_DIALOG -> {
                dataBinding.appCompatTextView.text =
                    getString(R.string.no_internet_connection_available)
                dataBinding.appCompatTextView2.visibility = View.VISIBLE
            }
            else -> {
                dataBinding.appCompatTextView.text =
                    arguments?.getString(AppConstants.MESSAGE)
                        ?: getString(R.string.something_went_wrong)
                dataBinding.appCompatTextView2.visibility = View.INVISIBLE
            }
        }
    }
}